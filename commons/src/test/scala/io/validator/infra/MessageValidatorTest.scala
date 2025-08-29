package io.validator.infra

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.*
import io.validator.core.Errors.{DomainError, PhishingDetected}
import io.validator.core.Model.ThreatLevel
import io.validator.effects.Now
import munit.FunSuite

import java.net.URI
import java.time.Instant
import scala.util.Try

class MessageValidatorTest extends FunSuite {

  test("findUrls suite") {
    val validator = MessageValidator(new UrlValidator {
      override def getThreatLevels(urls: Set[String])(using Now): IO[Map[String, ThreatLevel]] = Map.empty.pure[IO]
    })

    test("empty urls list when no urls are in given message") {
      val message = "this message contains no urls"
      val obtained = validator.findUrls(message)
      val expected = Set.empty[String]
      assertEquals(obtained, expected)
    }

    test("properly finds a url in given message") {
      val message =
        "I've got a message containing https://www.a-url.pl, which is a url"
      val obtained = validator.findUrls(message)
      val expected = Set("https://www.a-url.pl")
      assertEquals(obtained, expected)
      // also check if found string really is a valid uri
      obtained.foreach { maybeUri =>
        val parsed = Try(URI.create(maybeUri))
        assert(parsed.isSuccess, s"got $maybeUri which is not a valid URI")
      }
    }

    test("properly finds all urls in given message") {
      val message =
        "I've got a message containing https://www.a-url.pl, https://www.a-url-2.pl. Both are urls"
      val obtained = validator.findUrls(message)
      val expected = Set("https://www.a-url.pl", "https://www.a-url-2.pl")
      assertEquals(obtained, expected)
      // also check if found string really is a valid uri
      obtained.foreach { maybeUri =>
        val parsed = Try(URI.create(maybeUri))
        assert(parsed.isSuccess, s"got $maybeUri which is not a valid URI")
      }
    }

    test("find urls with different schemas") {
      val message =
        s"""|
            |https://www.a-url.pl
            |http://www.a-url.pl
            |https://a-url.pl
            |http://a-url.pl
            |www.a-url.pl
            |a-url.pl
            |a-url.com.pl
            |""".stripMargin
      val obtained = validator.findUrls(message)
      val expected = Set(
        "https://www.a-url.pl",
        "http://www.a-url.pl",
        "https://a-url.pl",
        "http://a-url.pl",
        "www.a-url.pl",
        "a-url.pl",
        "a-url.com.pl"
      )
      assertEquals(obtained, expected)
      // also check if found string really is a valid uri
      obtained.foreach { maybeUri =>
        val parsed = Try(URI.create(maybeUri))
        assert(parsed.isSuccess, s"got $maybeUri which is not a valid URI")
      }
    }
  }

  test("validate urls suite") {
    given Now = Now(Instant.EPOCH)
    val threatLevel = ThreatLevel.Unsafe(Vector("TESTING"))
    val alwaysUnsafe = MessageValidator(new UrlValidator {
      override def getThreatLevels(urls: Set[String])(using Now): IO[Map[String, ThreatLevel]] = {
        urls.map(url => url -> threatLevel).toMap.pure[IO]
      }
    })
    val alwaysSafe = MessageValidator(new UrlValidator {
      override def getThreatLevels(urls: Set[String])(using Now): IO[Map[String, ThreatLevel]] = {
        urls.map(url => url -> ThreatLevel.Safe()).toMap.pure[IO]
      }
    })

    test("handle safe message") {
      val message =
        "I've got a message containing https://www.a-url.pl, https://www.a-url-2.pl. Both are urls"
      val obtained = alwaysSafe.validate(message).unsafeRunSync()
      val expected = ().asRight[DomainError]
      assertEquals(obtained, expected)
    }

    test("handle unsafe message") {
      val message =
        "I've got a message containing https://www.a-url.pl, https://www.a-url-2.pl. Both are urls"
      val obtained = alwaysUnsafe.validate(message).unsafeRunSync()
      val expected = PhishingDetected(
        Map(
          "https://www.a-url.pl" -> "[TESTING]",
          "https://www.a-url-2.pl" -> "[TESTING]"
        )
      ).asLeft[Unit].leftWiden[DomainError]
      assertEquals(obtained, expected)
    }
  }
}
