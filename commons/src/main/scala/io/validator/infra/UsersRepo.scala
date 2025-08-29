package io.validator.infra

import cats.effect.IO
import io.validator.core.Model.UserId
import io.validator.core.User

trait UsersRepo {
  def getOrInit(id: UserId): IO[User]
  def save(user: User): IO[Unit]
}
