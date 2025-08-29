package io.validator.effects

import cats.effect.IO

import java.time.Instant

trait Time {
  def now: IO[Instant]
}

object Time {
  def apply()(using Time): Time = summon[Time]

  def default: Time = new Time {
    override def now: IO[Instant] = IO(Instant.now())
  }
}
