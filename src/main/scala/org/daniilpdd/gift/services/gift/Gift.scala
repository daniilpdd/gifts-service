package org.daniilpdd.gift.services.gift

import org.daniilpdd.gift.services.repositories.{GiftRepository, GiftRepositoryError}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
import zio.macros.accessible
import zio.{IO, Random, ZIO, ZLayer}

case class Gift(id: Int, name: String, price: Double)
object Gift {
  implicit val jsonEncoder: JsonEncoder[Gift] = DeriveJsonEncoder.gen[Gift]
}

@accessible
trait GiftService {
  def add(name: String, price: Double): IO[GiftRepositoryError, Gift]
  def get(id: Int): IO[GiftRepositoryError, Gift]
  def getAll: IO[GiftRepositoryError, Seq[Gift]]
}

object GiftService {
  val live: ZLayer[GiftRepository with Random, Nothing, GiftServiceLive] = ZLayer.fromZIO {
    for {
      gdb <- ZIO.service[GiftRepository]
      rnd <- ZIO.service[Random]
    } yield GiftServiceLive(gdb, rnd)
  }
}

case class GiftServiceLive(giftDataBase: GiftRepository, random: Random) extends GiftService {
  override def add(name: String, price: Double): IO[GiftRepositoryError, Gift] =
  for {
    id <- random.nextInt
    gift <- ZIO.succeed(Gift(id, name, price))
    _ <- giftDataBase.save(gift)
  } yield gift

  override def get(id: Int): IO[GiftRepositoryError, Gift] = giftDataBase.loadById(id)

  override def getAll: IO[GiftRepositoryError, Seq[Gift]] = giftDataBase.loadAll()
}


