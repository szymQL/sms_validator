package io.validator.core

object Errors {

  class DomainError(
      val message: String,
      val args: Map[String, String] = Map.empty
  ) extends Exception(message)

  case class PermissionDenied() extends DomainError("User hasn't granted permission to validate messages")

  case class PhishingDetected(threats: Map[String, String])
      extends DomainError(s"Phishing urls has been detected", threats)
}
