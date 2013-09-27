package fly.reactivemongo.evolutions

import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentWriter

case class FormatterWithEvolution[T](
  from: BSONDocument => T,
  to: T => BSONDocument,
  evolutions: Evolution*) extends BSONDocumentReader[T] with BSONDocumentWriter[T] {

  val sortedEvolutions = evolutions.sortBy(_.version)
  
  val version =
    if (evolutions.isEmpty) 0 else sortedEvolutions.last.version

  def read(doc: BSONDocument): T = from(evolve(doc))
  def write(obj: T): BSONDocument = to(obj).add("_version" -> version)

  def evolve(doc: BSONDocument): BSONDocument =
    sortedEvolutions.foldLeft(doc) {
      (previous, evolution) => evolution.transform(previous)
    }
}