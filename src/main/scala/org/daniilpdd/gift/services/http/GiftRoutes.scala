package org.daniilpdd.gift.services.http

import org.daniilpdd.gift.services.gift.Gift._
import org.daniilpdd.gift.services.gift.{Gift, GiftService}
import org.daniilpdd.gift.services.repositories.GiftRepositoryError
import zio.http.{Handler, Method, Request, Response, Routes, Status, handler}
import zio.json.{DecoderOps, DeriveJsonDecoder, EncoderOps, JsonDecoder}
import zio.macros.accessible
import zio.{IO, ZIO, ZLayer}

case class GiftDto(name: String, price: Double)
object GiftDto {

  implicit val jsonDecoder: JsonDecoder[GiftDto] = DeriveJsonDecoder.gen[GiftDto]
}

@accessible
trait GiftRoutes {
  def routes(): Routes[Any, Response]
}

object GiftRoutes {
  val live: ZLayer[GiftService, Nothing, GiftRoutesLive] = ZLayer.fromFunction(GiftRoutesLive.apply _)
}

case class GiftRoutesLive(giftService: GiftService) extends GiftRoutes {
  def wrapResponse(zioRes: IO[GiftRepositoryError, String]): ZIO[Any, Response, Response] = {
    zioRes.mapBoth(_ => Response.error(Status.InternalServerError), x => Response.json(x))
  }

  override def routes(): Routes[Any, Response] = Routes(
    Method.GET / "gifts" -> Handler.fromZIO(wrapResponse(giftService.getAll.map(_.toJson))),
    Method.GET / "gift" -> handler { req: Request =>
      (for {
        id <- req.queryParamToZIO[Int]("id")
        json <- giftService.get(id).map(_.toJson)
      } yield Response.json(json)).orElseFail(Response.error(Status.InternalServerError))
    },
    Method.POST / "gift" -> handler { req: Request =>
      (for {
        str <- req.body.asString
        gift <- ZIO.fromEither(str.fromJson[GiftDto])
        _ <- giftService.add(gift.name, gift.price)
      } yield Response.ok).orElseFail(Response.error(Status.InternalServerError))
    }
  )
}