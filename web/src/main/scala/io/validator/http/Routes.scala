package io.validator.http

import cats.effect.IO
import io.validator.app.*
import io.validator.effects.{RequiresNow, Time}
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter

class Routes(
    endpoints: Endpoints,
    permissionsUseCase: RequiresNow[PermissionsUseCase],
    saveSmsUseCase: RequiresNow[SaveSmsUseCase]
)(using Time) {

  def routes: HttpRoutes[IO] =
    Http4sServerInterpreter[IO]().toRoutes(serverEndpoints)

  private def serverEndpoints = List(
    grantPermissions,
    revokePermissions,
    saveSms
  )

  private def grantPermissions = endpoints.grantPermissions.serverLogic { userId =>
    permissionsUseCase.initNow().flatMap(_.grant(userId).attempt)
  }

  private def revokePermissions = endpoints.revokePermissions.serverLogic { userId =>
    permissionsUseCase.initNow().flatMap(_.revoke(userId).attempt)
  }

  private def saveSms = endpoints.saveSms.serverLogic { request =>
    saveSmsUseCase.initNow().flatMap(_.trySave(request.toDomain))
  }
}
