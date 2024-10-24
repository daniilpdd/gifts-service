package org.daniilpdd.pg

import zio.macros.accessible
import zio.{Console, Task, ZIO, ZLayer}

@accessible
trait Printer {
  def print(msg: String): Task[Unit]
}

object Printer {
  val live: ZLayer[Console, Nothing, PrinterLive] = ZLayer {
    for {
      c <- ZIO.service[Console]
    } yield PrinterLive(c)
  }
}

case class PrinterLive(console: Console) extends Printer {
  override def print(msg: String): Task[Unit] = console.printLine(msg)
}