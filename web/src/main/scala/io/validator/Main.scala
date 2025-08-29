package io.validator

import cats.effect.{IO, IOApp}
import io.validator.effects.Logging
import io.validator.http.HttpServer
import sttp.client4.httpclient.cats.HttpClientCatsBackend

object Main extends IOApp.Simple with Logging {

  override def run: IO[Unit] = (for {
    cfg <- AppConfig.load.toResource
    _ <- logger.info(s"Starting ${cfg.appName}").toResource
    sttp <- HttpClientCatsBackend.resource[IO]()
    appCtx <- AppCtx(cfg, sttp)
    _ <- HttpServer.resource(cfg, appCtx)
  } yield ()).useForever
}
