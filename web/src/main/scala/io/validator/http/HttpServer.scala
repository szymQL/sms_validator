package io.validator.http

import cats.effect.{IO, Resource}
import com.comcast.ip4s.{Host, Port, port}
import io.validator.{AppConfig, AppCtx}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}

object HttpServer {

  def resource(cfg: AppConfig, ctx: AppCtx): Resource[IO, Server] =
    val httpApp = Router(
      "/" -> ctx.routes.routes
    ).orNotFound

    EmberServerBuilder
      .default[IO]
      .withHostOption(Host.fromString(cfg.http.host))
      .withPort(Port.fromInt(cfg.http.port).getOrElse(port"8080"))
      .withHttpApp(httpApp)
      .build

}
