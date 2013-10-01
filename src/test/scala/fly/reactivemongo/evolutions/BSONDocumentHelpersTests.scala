package fly.reactivemongo.evolutions

import org.specs2.mutable.Specification
import reactivemongo.bson.BSONDocument

object BSONDocumentHelpersTests extends Specification {

  implicit class BSONDocumentTestEnhancements(doc: BSONDocument) {
    def ===(other: BSONDocument) =
      doc.stream.map(_.get) must containAllOf(other.stream.map(_.get))
  }

  "BSONDocumentHelpers" should {

    "provide implicit enhancements for" >> {

      import BSONDocumentHelpers._

      "removing keys" >> {

        "when they exist" in {
          doc123.remove("two", "three") === doc1
        }

        "when they don't exist" in {
          doc1.remove("two", "three") === doc1
        }
      }

      //NoSuchElementException
      "renaming keys" in {

        "when they exist" in {
          doc123.rename("two" -> "three", "three" -> "four") === doc134
        }

        "when they do not exist, throw an Exception" in {
          doc1.rename("two" -> "three", "three" -> "four") must throwA[KeyNotFoundException].like {
            case KeyNotFoundException(key) => key === "two"
          }
          doc2.rename("two" -> "three", "three" -> "four") must throwA[KeyNotFoundException].like {
            case KeyNotFoundException(key) => key === "three"
          }
        }
      }

      "easy access of required fields" in {

        "when they exist" in {
          doc1[Int]("one") === 1
        }

        "when they don't exist, throw an Exception" in {
          doc1[Int]("two") must throwA[KeyNotFoundException].like {
            case KeyNotFoundException(key) => key === "two"
          }
        }
      }
    }
  }

  val doc1 = BSONDocument("one" -> 1)
  val doc2 = BSONDocument("two" -> 2)
  val doc123 = BSONDocument("one" -> 1, "two" -> 2, "three" -> 3)
  val doc134 = BSONDocument("one" -> 1, "three" -> 2, "four" -> 3)
}