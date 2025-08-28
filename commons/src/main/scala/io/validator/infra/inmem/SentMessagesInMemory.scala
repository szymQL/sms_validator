package io.validator.infra.inmem

import cats.effect.{IO, Ref, Resource}
import io.validator.core.SentMessage
import io.validator.infra.SentMessagesRepo

class SentMessagesInMemory(ref: Ref[IO, Vector[SentMessage]]) extends SentMessagesRepo {

  override def save(msg: SentMessage): IO[Unit] = ref.update(_.appended(msg))
}

object SentMessagesInMemory {
  def resource: Resource[IO, SentMessagesInMemory] =
    Ref.of[IO, Vector[SentMessage]](Vector.empty).map(SentMessagesInMemory(_)).toResource
}
