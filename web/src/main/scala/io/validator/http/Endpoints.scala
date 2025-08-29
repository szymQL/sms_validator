package io.validator.http

import cats.implicits.*
import io.circe.{Decoder, Encoder, Codec as CirceCodec}
import io.circe.Codec.AsObject
import io.validator.core.Errors.DomainError
import io.validator.core.Model.UserId
import io.validator.core.SentMessage
import io.validator.http.Endpoints.{DomainEndpoint, SaveSmsRequest, baseEndpoint}
import sttp.model.StatusCode
import sttp.tapir.SchemaType.{SProduct, SProductField}
import sttp.tapir.*
import sttp.tapir.json.circe.*
import io.github.arainko.ducktape.*

class Endpoints() {

  given [A] => Schema[A] = Schema.any

  val grantPermissions: DomainEndpoint[UserId, Unit] =
    baseEndpoint
      .description("grant permissions for gathering info")
      .post
      .in("permissions" / "grant" / path[UserId]("userId"))
      .out(jsonBody[Unit])

  val revokePermissions: DomainEndpoint[UserId, Unit] =
    baseEndpoint
      .description("revoke permissions for gathering info")
      .post
      .in("permissions" / "revoke" / path[UserId]("userId"))
      .out(jsonBody[Unit])

  val saveSms: DomainEndpoint[SaveSmsRequest, Unit] =
    baseEndpoint
      .description("try saving the sms after validation")
      .post
      .in("sms")
      .in(jsonBody[SaveSmsRequest])
      .out(jsonBody[Unit])

}

object Endpoints {
  case class SaveSmsRequest(sender: UserId, recipient: UserId, message: String) derives AsObject {
    def toDomain: SentMessage = this.to[SentMessage]
  }

  type DomainEndpoint[I, O] = PublicEndpoint[I, Throwable, O, Any]

  private case class ErrorView(message: String, args: Map[String, String]) extends Exception(message) derives AsObject

  private val errorSchema: Schema[ErrorView] =
    Schema(
      SProduct(
        List(
          SProductField(FieldName("message"), Schema.string, _.message.some),
          SProductField(FieldName("args"), Schema.schemaForMap[String], _.args.some)
        )
      )
    )

  given CirceCodec[DomainError] = CirceCodec.from(
    Decoder[ErrorView].map(err => DomainError(err.message, err.args)),
    Encoder[ErrorView].contramap(err => ErrorView(err.message, err.args))
  )

  given CirceCodec[Throwable] = CirceCodec.from(
    Decoder[ErrorView].map(th => ErrorView(th.getMessage, Map.empty)),
    Encoder[ErrorView].contramap(_ => ErrorView("UNKNOWN_ERROR", Map.empty))
  )

  given Schema[DomainError] = errorSchema.as[DomainError]
  given Schema[Throwable] = errorSchema.as[Throwable]

  val baseEndpoint: PublicEndpoint[Unit, Throwable, Unit, Any] =
    endpoint
      .errorOut(
        oneOf[Throwable](
          oneOfVariant[DomainError](StatusCode.UnprocessableEntity, jsonBody[DomainError]),
          oneOfVariant[Throwable](StatusCode.InternalServerError, jsonBody[Throwable])
        )
      )
}
