package org.daniilpdd.gift.services.db

import org.daniilpdd.gift.services.gift.Gift
import zio.macros.accessible
import zio.{Console, IO, ZIO, ZLayer}

sealed trait GiftDataBaseError

case object SaveError extends GiftDataBaseError

case object ExistError extends GiftDataBaseError

case object LoadError extends GiftDataBaseError

@accessible
trait GiftDataBase {
  def save(gift: Gift): IO[GiftDataBaseError, Unit]

  def loadAll(): IO[GiftDataBaseError, Seq[Gift]]

  def loadById(id: Int): IO[GiftDataBaseError, Gift]
}

object GiftDataBase {
  val live = ZLayer.fromZIO {
    for {
      c <- ZIO.service[Console]
    } yield GiftDataBaseLive(c)
  }
}

case class GiftDataBaseLive(console: Console) extends GiftDataBase {
  private val mmap = scala.collection.mutable.Map.empty[Int, Gift]

  override def save(gift: Gift): IO[GiftDataBaseError, Unit] =
    for {
      exist <- ZIO.succeed(mmap.contains(gift.id))
      _ <- if (exist) ZIO.fail(ExistError) else ZIO.succeed(mmap.addOne((gift.id, gift)))
      _ <- console.printLine(s"save gift ${gift.id}").orElseFail(SaveError)
    } yield ()

  override def loadAll(): IO[GiftDataBaseError, Seq[Gift]] =
    for {
      gifts <- ZIO.attempt(mmap.values.toSeq).orElseFail(LoadError)
    } yield gifts

  override def loadById(id: Int): IO[GiftDataBaseError, Gift] =
    for {
      gift <- ZIO.attempt(mmap(id)).orElseFail(LoadError)
    } yield gift
}
