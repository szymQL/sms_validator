package io.validator.effects

import cats.effect.IO

import java.time.Instant

case class Now(get: Instant)

object Now {
  def apply()(using Now): Now = summon[Now]
  def create()(using Time): IO[Now] = Time().now.map(apply)
}
