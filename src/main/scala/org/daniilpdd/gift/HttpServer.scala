package org.daniilpdd.gift

import org.daniilpdd.gift.services.gift.GiftService
import org.daniilpdd.gift.services.http.GiftRoutes
import org.daniilpdd.gift.services.repositories.GiftRepository
import zio.http.Server
import zio.{Console, Random, Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object HttpServer extends ZIOAppDefault {
  val program = for {
    routes <- GiftRoutes.routes()
    _ <- Server.serve(routes)
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    program.provide(
      ZLayer.succeed(Console.ConsoleLive),
      GiftService.live,
      GiftRepository.live,
      ZLayer.succeed(Random.RandomLive),
      GiftRoutes.live,
      Server.defaultWithPort(8080)
    )
  }
}
