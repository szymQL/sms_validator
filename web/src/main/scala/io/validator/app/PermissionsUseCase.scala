package io.validator.app

import cats.effect.IO
import io.validator.core.Model.UserId
import io.validator.effects.Now
import io.validator.infra.UsersRepo

class PermissionsUseCase(usersRepo: UsersRepo)(using Now) {

  def grant(userId: UserId): IO[Unit] = for {
    user <- usersRepo.getOrInit(userId)
    _ <- IO.whenA(!user.hasGrantedPermission)(
      usersRepo.save(user.withPermissionGranted(Now().get))
    )
  } yield ()

  def revoke(userId: UserId): IO[Unit] = for {
    user <- usersRepo.getOrInit(userId)
    _ <- IO.whenA(user.hasGrantedPermission)(
      usersRepo.save(user.withPermissionRevoked(Now().get))
    )
  } yield ()
}
