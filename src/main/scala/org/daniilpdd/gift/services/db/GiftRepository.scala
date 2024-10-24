package org.daniilpdd.gift.services.db

import org.daniilpdd.gift.services.gift.Gift
import zio.macros.accessible
import zio.{Console, IO, ZIO, ZLayer}

sealed trait GiftRepositoryError

case object SaveError extends GiftRepositoryError

case object ExistError extends GiftRepositoryError

case object LoadError extends GiftRepositoryError

@accessible
trait GiftRepository {
  def save(gift: Gift): IO[GiftRepositoryError, Unit]

  def loadAll(): IO[GiftRepositoryError, Seq[Gift]]

  def loadById(id: Int): IO[GiftRepositoryError, Gift]
}

object GiftRepository {
  val live = ZLayer.fromZIO {
    for {
      c <- ZIO.service[Console]
    } yield GiftRepositoryLive(c)
  }
}

case class GiftRepositoryLive(console: Console) extends GiftRepository {
  private val mmap = scala.collection.mutable.Map.empty[Int, Gift]

  override def save(gift: Gift): IO[GiftRepositoryError, Unit] =
    for {
      exist <- ZIO.succeed(mmap.contains(gift.id))
      _ <- if (exist) ZIO.fail(ExistError) else ZIO.succeed(mmap.addOne((gift.id, gift)))
      _ <- console.printLine(s"save gift ${gift.id}").orElseFail(SaveError)
    } yield ()

  override def loadAll(): IO[GiftRepositoryError, Seq[Gift]] =
    for {
      gifts <- ZIO.attempt(mmap.values.toSeq).orElseFail(LoadError)
    } yield gifts

  override def loadById(id: Int): IO[GiftRepositoryError, Gift] =
    for {
      gift <- ZIO.attempt(mmap(id)).orElseFail(LoadError)
    } yield gift
}
