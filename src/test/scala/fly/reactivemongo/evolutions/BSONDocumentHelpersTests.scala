package fly.reactivemongo.evolutions

import org.specs2.mutable.Specification
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.exceptions.DocumentKeyNotFound
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import BSONDocumentHelpers._
import reactivemongo.bson.Producer
import org.specs2.matcher.MatchResult

object BSONDocumentHelpersTests extends Specification {

  implicit class BSONDocumentTestEnhancements(doc: BSONDocument) {
    def ===(other: BSONDocument) =
      doc.stream.map(_.get) must containAllOf(other.stream.map(_.get))
  }

  "BSONDocumentHelpers" should {

    "provide implicit enhancements for" >> {

      "removing keys" >> {

        "when they exist" in {
          doc123.remove("two", "three") === doc1
        }

        "when they don't exist, throw exception" in {
          expectKeyNotFound("two") {
            doc1.remove("two", "three")
          }
          expectKeyNotFound("three") {
            doc2.remove("two", "three")
          }
        }
      }

      "renaming keys" >> {

        "when they exist" in {
          doc123.rename("two" -> "three", "three" -> "four") === doc134
        }

        "when they do not exist, throw an Exception" in {
          expectKeyNotFound("two") {
            doc1.rename("two" -> "three", "three" -> "four")
          }
          expectKeyNotFound("three") {
            doc2.rename("two" -> "three", "three" -> "four")
          }
        }
      }

      "updating keys" >> {

        "when they exists" in {
          doc123.update("one" -> 2, "two" -> 3, "three" -> 4) === BSONDocument("one" -> 2, "two" -> 3, "three" -> 4)
        }

        "when they do not exist, throw an exception" in {
          expectKeyNotFound("two") {
            doc1.update("two" -> 2, "three" -> 3)
          }
          expectKeyNotFound("three") {
            doc2.update("two" -> 2, "three" -> 3)
          }
        }
      }

      "easy access of required fields" >> {

        "when they exist" in {
          doc1[Int]("one") === 1
        }

        "when they don't exist, throw an Exception" in {
          expectKeyNotFound("two") {
            doc1[Int]("two")
          }
        }

        "when they they have an incorrect type, throw an Exception" in {
          doc1[String]("one") must throwA[ClassCastException]
        }

        "when accessing an object that is missing a key, throw the correct exception" in {

          val doc = BSONDocument("one" -> BSONDocument())

          case class Test(name: String)
          implicit val bsonFormatter = new BSONDocumentReader[Test] {
            def read(doc: BSONDocument) = Test(doc[String]("name"))
          }

          expectKeyNotFound("name") {
            doc[Test]("one")
          }
        }
      }
    }
  }

  def expectKeyNotFound[T](expectedKey: String)(code: => T): MatchResult[T] =
    code must throwA[DocumentKeyNotFound].like {
      case DocumentKeyNotFound(key) => key === expectedKey
    }

  val doc1 = BSONDocument("one" -> 1)
  val doc2 = BSONDocument("two" -> 2)
  val doc123 = BSONDocument("one" -> 1, "two" -> 2, "three" -> 3)
  val doc134 = BSONDocument("one" -> 1, "three" -> 2, "four" -> 3)
}