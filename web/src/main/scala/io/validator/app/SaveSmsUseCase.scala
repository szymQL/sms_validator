package io.validator.app

import cats.effect.IO
import cats.implicits.*
import io.validator.AppConfig
import io.validator.core.Errors.{DomainError, PermissionDenied}
import io.validator.core.SentMessage
import io.validator.effects.Now
import io.validator.infra.{MessageValidator, SentMessagesRepo, UsersRepo}

class SaveSmsUseCase(
    usersRepo: UsersRepo,
    messagesRepo: SentMessagesRepo,
    messageValidator: MessageValidator
)(using Now) {

  def trySave(messageToSave: SentMessage): IO[Either[DomainError, Unit]] = for {
    user <- usersRepo.getOrInit(messageToSave.recipient)
    userOrErr = Either
      .cond(user.hasGrantedPermission, user, PermissionDenied())
      .leftWiden[DomainError]
    validationResult <- userOrErr.flatTraverse(_ => messageValidator.validate(messageToSave.message))
    result <- validationResult.traverse(_ => messagesRepo.save(messageToSave))
  } yield result
}
