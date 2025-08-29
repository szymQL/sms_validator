package io.validator.core

import io.circe.{Codec, Decoder, Encoder}
import pureconfig.ConfigReader
import sttp.tapir.CodecFormat.TextPlain
import sttp.tapir.Codec as TCodec

object Model {

  opaque type UserId = String

  object UserId {
    def apply(id: String): UserId = id

    given Codec[UserId] = Codec.from(Decoder.decodeString, Encoder.encodeString)
    given TCodec[String, UserId, TextPlain] = TCodec.string
  }

  opaque type Score = Double

  object Score {
    def apply(score: Double): Score = score

    given Codec[Score] = Codec.from(Decoder.decodeDouble, Encoder.encodeDouble)
    given ConfigReader[Score] = ConfigReader.doubleConfigReader
  }

  enum ThreatLevel {
    def isUnsafe: Boolean = this match {
      case _: ThreatLevel.Safe   => false
      case _: ThreatLevel.Unsafe => true
    }

    case Safe()
    case Unsafe(threatTypes: Vector[String])
  }

}
