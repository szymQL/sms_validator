package infra

import io.validator.infra.MessageValidator
import munit.FunSuite

import java.net.URI
import scala.util.Try

class MessageValidatorTest extends FunSuite {

  private val validator = MessageValidator()

  test("findUrls suite") {
    test("empty urls list when no urls are in given message") {
      val message = "this message contains no urls"
      val obtained = validator.findUrls(message)
      val expected = Vector.empty[String]
      assertEquals(obtained, expected)
    }

    test("properly finds a url in given message") {
      val message =
        "I've got a message containing https://www.a-url.pl, which is a url"
      val obtained = validator.findUrls(message)
      val expected = Vector("https://www.a-url.pl")
      assertEquals(obtained, expected)
      //also check if found string really is a valid uri
      obtained.foreach { maybeUri =>
        val parsed = Try(URI.create(maybeUri))
        assert(parsed.isSuccess, s"got $maybeUri which is not a valid URI")
      }
    }

    test("properly finds all urls in given message") {
      val message =
        "I've got a message containing https://www.a-url.pl, https://www.a-url-2.pl. Both are urls"
      val obtained = validator.findUrls(message)
      val expected = Vector("https://www.a-url.pl", "https://www.a-url-2.pl")
      assertEquals(obtained, expected)
      //also check if found string really is a valid uri
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
      val expected = Vector(
        "https://www.a-url.pl",
        "http://www.a-url.pl",
        "https://a-url.pl",
        "http://a-url.pl",
        "www.a-url.pl",
        "a-url.pl",
        "a-url.com.pl"
      )
      assertEquals(obtained, expected)
      //also check if found string really is a valid uri
      obtained.foreach { maybeUri =>
        val parsed = Try(URI.create(maybeUri))
        assert(parsed.isSuccess, s"got $maybeUri which is not a valid URI")
      }
    }
  }
}
