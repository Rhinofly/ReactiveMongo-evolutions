package fly.reactivemongo.evolutions

import reactivemongo.bson.BSONDocument

case class Evolution(version:Int)(val transform:BSONDocument => BSONDocument)