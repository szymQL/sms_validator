package io.validator

import cats.effect.IO
import io.validator.AppConfig.*
import pureconfig.{ConfigReader, ConfigSource}

import java.time.Duration

case class AppConfig(appName: String, http: HttpConfig, webRiskClient: WebRiskClientConfig) derives ConfigReader

object AppConfig {
  case class HttpConfig(host: String, port: Int)
  case class WebRiskClientConfig(apiKey: String, defaultExpiration: Duration)

  def load: IO[AppConfig] = IO(ConfigSource.default.loadOrThrow[AppConfig])
}
