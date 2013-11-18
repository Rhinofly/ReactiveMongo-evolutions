package fly.reactivemongo.evolutions

import reactivemongo.bson.BSONDocument
import scala.util.Success
import scala.util.Try
import reactivemongo.bson.BSONReader
import reactivemongo.bson.BSONValue
import reactivemongo.bson.Producer
import reactivemongo.bson.exceptions.DocumentKeyNotFound
import reactivemongo.bson.BSONWriter

object BSONDocumentHelpers {

  implicit class BSONDocumentEnhancements(doc: BSONDocument) {

    def remove(keys: String*): BSONDocument = {
      checkExistence(keys)
      val filteredElements =
        doc.stream.filter {
          case Success((key, _)) if (keys contains key) => false
          case _ => true
        }

      BSONDocument(filteredElements)
    }

    def rename(keys: (String, String)*): BSONDocument = {
      val oldKeys = keys.map { case (oldKey, _) => oldKey }

      val withoutElements = remove(oldKeys: _*)

      val renamedElements = keys.map(renameKeys)

      withoutElements.add(renamedElements: _*)
    }

    private def renameKeys(keys: (String, String)): Producer[(String, BSONValue)] = {
      val (oldKey, newKey) = keys
      doc.getTry(oldKey)
        .map(newKey -> _)
        .get
    }

    def apply[T](key: String)(implicit reader: BSONReader[_ <: BSONValue, T]): T =
      doc.getAsTry[T](key).get

    def update(producers: Producer.NameOptionValueProducer*): BSONDocument = {
      val keys = producers.map { case Producer.NameOptionValueProducer((key, _)) => key }
      doc.remove(keys: _*).add(producers: _*)
    }

    def update[A, B](updateMethods: (String, A => B)*)(implicit reader: BSONReader[_ <: BSONValue, A], writer: BSONWriter[B, _ <: BSONValue]): BSONDocument = {
      val producers = updateMethods.map {
        case (key, method) =>
          val producer: Producer.NameOptionValueProducer =
            key -> method(apply[A](key))
          producer
      }

      update(producers: _*)
    }

    private def checkExistence(keys: Seq[String]): Unit =
      keys.foreach(key => doc.getTry(key).get)
  }
}