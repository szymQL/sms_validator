package io.validator.infra

import cats.effect.IO
import io.validator.core.Errors.DomainError
import io.validator.core.Model.Score

import scala.util.matching.Regex

trait MessageValidator(urlValidator: UrlValidator) {
  private def uriRegex: Regex = "[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)".r
  
  def findUrls(message: String): Vector[String] = uriRegex.findAllIn(message).toVector

  def validate(message: String, minScore: Score): IO[Either[DomainError, Score]]
}
