package io.validator.infra

import io.validator.effects.Now

trait UrlValidator {
  
  def getScore(url: String)(using Now): Option[Long]

}
