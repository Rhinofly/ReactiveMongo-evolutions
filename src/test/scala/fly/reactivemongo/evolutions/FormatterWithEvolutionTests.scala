package fly.reactivemongo.evolutions

import org.specs2.mutable.Specification
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import reactivemongo.bson.BSONDocument

object FormatterWithEvolutionTests extends Specification {

  "FormatterWithEvolution" should {

    "be a bson document reader and writer" in {
      formatter must beAnInstanceOf[BSONDocumentReader[Test] with BSONDocumentWriter[Test]]
    }

    "add the version to the bson" in {
      val result = formatter.write(testInstance)

      result.stream must containTheSameElementsAs(testDocument.stream)
    }

    "read the document" in {
      formatter.read(testDocument) === testInstance
    }

    "have the correct version (sort the evolutions)" in {
      val formatter =
        FormatterWithEvolution[Test](
          from = { doc =>
            Test(doc.getAs[String]("name").getOrElse("<noname>"))
          },
          to = { test =>
            BSONDocument("name" -> test.name)
          },
          evolution2,
          evolution1)

      formatter.version === 2
    }

    "apply the evolutions" in {
      val formatter =
        FormatterWithEvolution[Test2](
          from = { doc =>
            Test2(doc.getAs[String]("name").get,
                doc.getAs[Int]("one").get,
                doc.getAs[Int]("two").get)
          },
          to = { test =>
            BSONDocument(
                "name" -> test.name,
                "one" -> test.one,
                "two" -> test.two
            )
          },
          evolution2,
          evolution1)

      val expected = 
        Test2("test", 1, 2)
      val result = formatter.read(testDocument)
      
      result === expected
    }
    
    "apply evolutions from a specific version" in {
      todo
    }
    
    "throw an error if the version is not present in the evolution list" in {
      todo
    }
  }

  case class Test(name: String)
  case class Test2(name: String, one:Int, two:Int)

  val formatter =
    FormatterWithEvolution[Test](
      from = { doc =>
        Test(doc.getAs[String]("name").getOrElse("<noname>"))
      },
      to = { test =>
        BSONDocument("name" -> test.name)
      })

  val testInstance = Test("test")
  val testDocument = BSONDocument("_version" -> 0, "name" -> "test")

  val evolution1 =
    Evolution(1) { doc =>
      doc.add("one" -> 1)
    }

  val evolution2 =
    Evolution(2) { doc =>
      val twoValue = doc.getAs[Int]("one").get + 1
      doc.add("two" -> twoValue)
    }
}