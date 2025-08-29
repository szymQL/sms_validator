package io.validator.effects

import cats.effect.IO
import org.typelevel.log4cats.*
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait Logging {

  def logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLoggerFromClass[IO](this.getClass)

}
