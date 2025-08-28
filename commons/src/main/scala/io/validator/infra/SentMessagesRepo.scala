package io.validator.infra

import cats.effect.IO
import io.validator.core.SentMessage

trait SentMessagesRepo {
  def save(msg: SentMessage): IO[Unit]
}
