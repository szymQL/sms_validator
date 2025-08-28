package io.validator.core

import io.circe.{Codec, Decoder, Encoder}
import pureconfig.ConfigReader

object Model {

  opaque type UserId = String

  object UserId {
    def apply(id: String): UserId = id

    given Codec[UserId] = Codec.from(Decoder.decodeString, Encoder.encodeString)
  }

  opaque type Score = Double

  object Score {
    def apply(score: Double): Score = score

    given Codec[Score] = Codec.from(Decoder.decodeDouble, Encoder.encodeDouble)
    given ConfigReader[Score] = ConfigReader.doubleConfigReader
  }

}
