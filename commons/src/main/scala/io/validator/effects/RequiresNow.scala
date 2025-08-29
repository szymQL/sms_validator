package io.validator.effects

import cats.effect.IO

class RequiresNow[+A](f: Now => A) {
  def apply()(using Now): A = f(Now())
  def initNow()(using Time): IO[A] = Now.create().map(f)
}

object RequiresNow {
  def apply[A](f: Now => A) = new RequiresNow(f)

  given [A](using Now): Conversion[RequiresNow[A], A] = _()
}
