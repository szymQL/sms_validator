package io.validator.infra

import cats.effect.IO
import cats.implicits.*
import io.validator.core.Model
import io.validator.effects.Now
import munit.FunSuite

import java.net.URI
import scala.util.Try

class MessageValidatorTest extends FunSuite {

  private val validator = MessageValidator(new UrlValidator {
    override def getThreatLevels(urls: Set[String])(using Now): IO[Map[String, Model.ThreatLevel]] = Map.empty.pure[IO]
  })

  test("findUrls suite") {
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
}
