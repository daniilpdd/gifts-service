package org.daniilpdd.gift

import org.daniilpdd.gift.services.db.GiftRepository
import org.daniilpdd.gift.services.gift.GiftService
import zio.{Console, Random, Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object Main extends ZIOAppDefault {
  val program = for {
    _ <- GiftService.add("router", 12500)
    _ <- GiftService.add("cake", 200)
    _ <- GiftService.add("phone", 55500)
    gifts <- GiftService.getAll
    _ <- Console.printLine(gifts)
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    program.provide(ZLayer.succeed(Console.ConsoleLive), GiftService.live, GiftRepository.live, ZLayer.succeed(Random.RandomLive))
  }
}
