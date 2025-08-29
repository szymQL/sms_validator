package io.validator.core

import Model.UserId

case class SentMessage(sender: UserId, recipient: UserId, message: String)
