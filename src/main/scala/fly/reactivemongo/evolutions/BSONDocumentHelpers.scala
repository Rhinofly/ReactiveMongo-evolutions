package fly.reactivemongo.evolutions

import reactivemongo.bson.BSONDocument
import scala.util.Success
import scala.util.Try
import reactivemongo.bson.BSONReader
import reactivemongo.bson.BSONValue
import reactivemongo.bson.Producer
import reactivemongo.bson.exceptions.DocumentKeyNotFound

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

      withoutElements.add(renamedElements:_*)
    }

    private def renameKeys(keys: (String, String)): Producer[(String, BSONValue)] = {
      val (oldKey, newKey) = keys
      doc.getTry(oldKey)
        .map(newKey -> _)
        .get
    }

    def apply[T](key: String)(implicit reader: BSONReader[_ <: BSONValue, T]): T =
      doc.getAsTry[T](key).get
      
    def update[T](producers: Producer.NameOptionValueProducer *):BSONDocument = {
      val keys = producers.map { case Producer.NameOptionValueProducer((key, _)) => key }
      doc.remove(keys:_*).add(producers:_*)
    }
    
    private def checkExistence(keys:Seq[String]):Unit =
      keys.foreach(key => doc.getTry(key).get)
  }
}