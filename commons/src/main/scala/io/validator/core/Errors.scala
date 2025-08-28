package io.validator.core

object Errors {

  class DomainError(val message: String) extends Exception(message)

  case class PermissionDenied()
      extends DomainError("User hasn't granted permission to validate messages")
}
