package io.validator.infra

import cats.effect.IO
import io.validator.core.Model.ThreatLevel
import io.validator.effects.Now

trait UrlValidator {

  def getThreatLevels(urls: Set[String])(using Now): IO[Map[String, ThreatLevel]]

}
