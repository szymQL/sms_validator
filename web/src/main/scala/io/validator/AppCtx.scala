package io.validator

import cats.effect.{IO, Resource}
import io.validator.app.*
import io.validator.effects.{RequiresNow, Time}
import io.validator.http.{Endpoints, Routes}
import io.validator.infra.MessageValidator
import io.validator.infra.http.HttpWebRiskClient
import io.validator.infra.inmem.{SentMessagesInMemory, UsersRepoInMemory}
import sttp.client4.Backend

case class AppCtx(routes: Routes) {}

object AppCtx {

  def apply(cfg: AppConfig, sttp: Backend[IO]): Resource[IO, AppCtx] = for {
    sentMessagesRepo <- SentMessagesInMemory.resource
    usersRepo <- UsersRepoInMemory.resource
    urlValidator <- HttpWebRiskClient.resource(sttp, cfg.webRiskClient.apiKey, cfg.webRiskClient.defaultExpiration)
    messageValidator = MessageValidator(urlValidator)

    given Time = Time.default

    permissionsUseCase = RequiresNow(PermissionsUseCase(usersRepo)(using _))

    saveSmsUseCase = RequiresNow(SaveSmsUseCase(usersRepo, sentMessagesRepo, messageValidator)(using _))

    endpoints = Endpoints()
    routes = Routes(endpoints, permissionsUseCase, saveSmsUseCase)
  } yield AppCtx(routes)
}
