package fly.reactivemongo.evolutions

import org.specs2.mutable.Specification
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import reactivemongo.bson.BSONDocument
import scala.util.Success

object FormatterWithEvolutionTests extends Specification {

  "FormatterWithEvolution" should {

    "be a bson document reader and writer" in {
      formatter must beAnInstanceOf[BSONDocumentReader[Test] with BSONDocumentWriter[Test]]
    }

    "add the version to the bson" in {
      val result = formatter.write(testInstance)

      result.stream must containTheSameElementsAs(testDocument0.stream)
    }

    "read the document" in {
      formatter.read(testDocument) === testInstance
    }

    "have the correct version (sort the evolutions)" in {
      formatter2.evolutions === Seq(evolution2, evolution1)
      formatter2.version === 2
    }

    "apply the evolutions" in {

      val expected = Test2("test", 1, 2)
      val result = formatter2.read(testDocument)

      result === expected
    }

    "apply evolutions from a specific version" in {

      val expected = Test2("test", 1, 2)
      val result = formatter2.read(testDocument1)

      result === expected
    }

    "throw an error if the version is not present in the evolution list" in {
      
      val formatter = FormatterWithEvolution[Test2](
        formatter2.from,
        formatter2.to,
        evolution2)

      formatter.read(testDocument) must throwA[formatter.InvalidVersionException].like {
        case formatter.InvalidVersionException(doc, description) =>
          (doc === testDocument) and
            (description must contain("0")) and
            (description must contain("1"))
      }
    }
  }

  case class Test(name: String)
  case class Test1(title: String, one: Int)
  case class Test2(title: String, one: Int, two: Int)

  lazy val formatter =
    FormatterWithEvolution[Test](
      from = { doc =>
        Test(doc.getAs[String]("name").getOrElse("<noname>"))
      },
      to = { test =>
        BSONDocument("name" -> test.name)
      })

  lazy val formatter2 =
    FormatterWithEvolution[Test2](
      from = { doc =>
        Test2(
          doc.getAs[String]("title").get,
          doc.getAs[Int]("one").get,
          doc.getAs[Int]("two").get)
      },
      to = { test =>
        BSONDocument(
          "title" -> test.title,
          "one" -> test.one,
          "two" -> test.two)
      },
      evolution2,
      evolution1)

  val testInstance = Test("test")
  val testDocument = BSONDocument("name" -> "test")
  val testDocument0 = BSONDocument("_version" -> 0, "name" -> "test")
  val testDocument1 = BSONDocument("_version" -> 1, "title" -> "test", "one" -> 1)

  val evolution1 =
    Evolution(1) { doc =>
      val name = doc.get("name").get

      val filteredElements =
        doc.stream.filter {
          case Success((key, value)) => key != "name"
          case _ => true
        }

      BSONDocument(filteredElements)
        .add("one" -> 1)
        .add("title" -> name)
    }

  val evolution2 =
    Evolution(2) { doc =>
      val twoValue = doc.getAs[Int]("one").get + 1
      doc.add("two" -> twoValue)
    }
}