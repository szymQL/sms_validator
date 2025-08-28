package io.validator

import cats.effect.{IO, IOApp}
import io.validator.http.HttpServer
import sttp.client4.httpclient.cats.HttpClientCatsBackend

object Main extends IOApp.Simple {

  override def run: IO[Unit] = (for {
    sttp <- HttpClientCatsBackend.resource[IO]()
    cfg <- AppConfig.load.toResource
    appCtx <- AppCtx(cfg, sttp)
    _ <- HttpServer.resource(cfg, appCtx)
  } yield ()).useForever
}
