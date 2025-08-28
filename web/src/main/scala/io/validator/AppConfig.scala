package io.validator

import io.validator.core.Model.Score
import pureconfig.ConfigReader

case class AppConfig(minScore: Score) derives ConfigReader
