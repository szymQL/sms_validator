package io.validator.infra.http

import cats.effect.{IO, Ref, Resource}
import cats.implicits.*
import io.circe.Codec.AsObject
import io.circe.derivation.Configuration
import io.validator.core.Model.*
import io.validator.effects.Now
import io.validator.infra.UrlValidator
import io.validator.infra.http.HttpWebRiskClient.ThreatsResponse
import sttp.client4.{Backend, basicRequest}
import sttp.client4.circe.asJsonAlways
import sttp.model.Uri.UriContext

import java.time.{Duration, Instant}

class HttpWebRiskClient(
    sttp: Backend[IO],
    cache: Ref[IO, Map[String, (ThreatsResponse, Instant)]],
    apiKey: String,
    defaultExpiration: Duration
) extends UrlValidator {

  override def getThreatLevels(urls: Set[String])(using Now): IO[Map[String, ThreatLevel]] =
    urls.toSeq
      .parTraverse { url =>
        for {
          maybeCached <- getCached(url)
          result <- maybeCached match {
            case Some(cached) => cached.pure[IO]
            case None         => getFromHttpAndSaveToCache(url)
          }
        } yield (url, result.asThreatLevel)
      }
      .map(_.toMap)

  private def getCached(url: String)(using Now): IO[Option[ThreatsResponse]] =
    cache.get.map(_.get(url) match {
      case Some((threatsResponse, expiresAt)) if expiresAt.isAfter(Now().get) => threatsResponse.some
      case _                                                                  => none
    })

  private def getFromHttpAndSaveToCache(url: String)(using Now): IO[ThreatsResponse] = {
    val uri = uri"https://webrisk.googleapis.com/v1/uris:search".addParams(
      ("uri", url),
      ("threatTypes", "SOCIAL_ENGINEERING"),
      ("threatTypes", "MALWARE"),
      ("threatTypes", "UNWANTED_SOFTWARE"),
      ("threatTypes", "SOCIAL_ENGINEERING_EXTENDED_COVERAGE")
    )
    val request = basicRequest.get(uri).header("X-goog-api-key", apiKey).response(asJsonAlways[ThreatsResponse])
    request
      .send(sttp)
      .flatMap(_.body match {
        case Left(err)       => IO.raiseError(err)
        case Right(response) =>
          cache.update(_.updated(url, (response, response.expiresAt(defaultExpiration)))).as(response)
      })
  }
}

object HttpWebRiskClient {

  case class ThreatsResponse(threat: Option[Threat]) derives AsObject {
    def expiresAt(defaultExpiration: Duration)(using Now): Instant =
      threat.fold(Now().get.plus(defaultExpiration))(_.expireTime)

    def asThreatLevel: ThreatLevel = threat match {
      case Some(t) => ThreatLevel.Unsafe(t.threatTypes)
      case None    => ThreatLevel.Safe()
    }
  }
  case class Threat(threatTypes: Vector[String], expireTime: Instant) derives AsObject

  def resource(sttp: Backend[IO], apiKey: String, defaultExpiration: Duration): Resource[IO, HttpWebRiskClient] =
    Ref
      .of[IO, Map[String, (ThreatsResponse, Instant)]](Map.empty)
      .map(HttpWebRiskClient(sttp, _, apiKey, defaultExpiration))
      .toResource

}
