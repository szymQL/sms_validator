package io.validator.infra

import cats.effect.IO
import io.validator.core.Errors.{DomainError, PhishingDetected}
import io.validator.core.Model.ThreatLevel
import io.validator.effects.Now

import scala.util.matching.Regex

class MessageValidator(urlValidator: UrlValidator) {
  private def uriRegex: Regex =
    "[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)".r

  def findUrls(message: String): Set[String] = uriRegex.findAllIn(message).toSet

  def validate(message: String)(using Now): IO[Either[DomainError, Unit]] =
    urlValidator.getThreatLevels(findUrls(message)).map { urls =>
      val unsafeUrls = urls.collect { case (url, ThreatLevel.Unsafe(threatTypes)) =>
        url -> threatTypes.mkString("[", ", ", "]")
      }
      Either.cond(unsafeUrls.isEmpty, (), PhishingDetected(unsafeUrls))
    }
}
