package io.validator.infra.inmem

import cats.effect.{IO, Ref, Resource}
import io.validator.core.Model.UserId
import io.validator.core.User
import io.validator.infra.UsersRepo

class UsersRepoInMemory(ref: Ref[IO, Map[UserId, User]]) extends UsersRepo {

  override def getOrInit(id: UserId): IO[User] =
    ref.get.map(_.getOrElse(id, User.init(id)))

  override def save(user: User): IO[Unit] = ref.update(_.updated(user.id, user))
}

object UsersRepoInMemory {
  def resource: Resource[IO, UsersRepoInMemory] =
    Ref.of[IO, Map[UserId, User]](Map.empty).map(UsersRepoInMemory(_)).toResource
}
