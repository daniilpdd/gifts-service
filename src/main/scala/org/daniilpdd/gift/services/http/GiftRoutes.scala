package org.daniilpdd.gift.services.http

import org.daniilpdd.gift.services.gift.Gift._
import org.daniilpdd.gift.services.gift.GiftService
import zio.http.{Method, Request, Response, Routes, Status, handler}
import zio.json.{DecoderOps, DeriveJsonDecoder, EncoderOps, JsonDecoder}
import zio.macros.accessible
import zio.{ZIO, ZLayer}

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
  override def routes(): Routes[Any, Response] = Routes(
    Method.GET / "gifts" -> handler(giftService.getAll.map(_.toJson).mapBoth(_ => Response.error(Status.InternalServerError), Response.json)),
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