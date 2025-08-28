package io.validator.core

import cats.syntax.option.*
import com.softwaremill.quicklens.*
import Model.UserId

import java.time.Instant

case class User(
    id: UserId,
    permissionGrantedAt: Option[Instant],
    permissionRevokedAt: Option[Instant]
) {
  def hasGrantedPermission: Boolean =
    permissionGrantedAt.fold(false)(grantedAt => permissionRevokedAt.fold(true)(_.isBefore(grantedAt)))

  def withPermissionGranted(at: Instant): User =
    this.modify(_.permissionGrantedAt).setTo(at.some)

  def withPermissionRevoked(at: Instant): User =
    this.modify(_.permissionRevokedAt).setTo(at.some)
}

object User {
  def init(id: UserId): User = User(id, none, none)
}
