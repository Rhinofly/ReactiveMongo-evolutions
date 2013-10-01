package fly.reactivemongo.evolutions

import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentWriter

case class FormatterWithEvolution[T](
  from: BSONDocument => T,
  to: T => BSONDocument,
  evolutions: Evolution*) extends BSONDocumentReader[T] with BSONDocumentWriter[T] {

  val VERSION_KEY = "_version"

  val sortedEvolutions = evolutions.sortBy(_.version)
  val versions = sortedEvolutions.map(_.version)

  val version = versions.lastOption.getOrElse(0)
  val minimalVersion = versions.headOption.map(_ - 1).getOrElse(0)

  def read(doc: BSONDocument): T = from(evolve(doc))
  def write(obj: T): BSONDocument = to(obj).add(VERSION_KEY -> version)

  def evolve(doc: BSONDocument): BSONDocument = {
    val docVersion = doc.getAs[Int](VERSION_KEY).getOrElse(0)
    val evolutionsToSkip = docVersion - minimalVersion
    
    if (evolutionsToSkip < 0) 
      throw InvalidVersionException(doc, s"There is no evolution to evolve the document from version $docVersion to $minimalVersion")
    
    val evolutions = sortedEvolutions.drop(evolutionsToSkip)
    
    evolutions.foldLeft(doc) {
      (previous, evolution) => evolution.transform(previous)
    }
  }
  
  case class InvalidVersionException(doc:BSONDocument, description:String) extends RuntimeException(description)
}