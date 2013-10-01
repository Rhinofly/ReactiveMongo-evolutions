package fly.reactivemongo.evolutions

import reactivemongo.bson.BSONDocument
import scala.util.Success
import scala.util.Try
import reactivemongo.bson.BSONReader
import reactivemongo.bson.BSONValue
import reactivemongo.bson.Producer

object BSONDocumentHelpers {

  case class KeyNotFoundException(key: String) extends NoSuchElementException(s"Could not find key $key")

  implicit class BSONDocumentEnhancements(doc: BSONDocument) {

    def remove(keys: String*): BSONDocument = {
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

      val renamedElements = keys.map(renameAndWrap)

      withoutElements.add(renamedElements:_*)
    }

    private def renameAndWrap(keys: (String, String)): Producer[(String, BSONValue)] = {
      val (oldKey, newKey) = keys
      doc.get(oldKey)
        .map(newKey -> _)
        .getOrElse(throw KeyNotFoundException(oldKey))
    }

    def apply[T](key: String)(implicit reader: BSONReader[_ <: BSONValue, T]): T =
      doc.getAs[T](key).getOrElse(throw KeyNotFoundException(key))
  }
}