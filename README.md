ReactiveMongo evolutions
=====================================================

Provides a solution for evolutions using [ReactiveMongo](http://reactivemongo.org/)


Installation
------------

``` scala
  val appDependencies = Seq(
    "nl.rhinofly" %% "ReactiveMongo-evolutions" % "1.0.0-SNAPSHOT"
  )
  
  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    resolvers += "Rhinofly Internal Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-release-local"
    // uncomment the following line to resolve snapshots
    //,resolvers += "Rhinofly Internal Snapshot Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-shapshots-local"
  )
```


Usage
-----

Evolutions
==========

The example below descibes a document that went through two evolutions. It started with only a name:

``` scala
{
  "name" : "<name>"
}
```

Name was changed to title:

``` scala
{
  "title" : "<name>"
}
```

Then description was added:

``` scala
{
  "title" : "<name>"
  "description" : "<description>"
}
```

``` scala
import fly.reactivemongo.evolutions.FormatterWithEvolution
import fly.reactivemongo.evolutions.BSONDocumentHelpers._

case class Test(title:String, description:String)

object Test {

  implicit val testBSONFormatter =  FormatterWithEvolution[Test](
      from = { doc =>
        Test(
          doc[String]("title"),
          doc[String]("description"))
      },
      to = { test =>
        BSONDocument(
          "title" -> test.title,
          "description" -> test.description)
      },
      Evolution(1) { doc =>
      	doc rename ("name" -> "title")
      },
      Evolution(2) { doc =>
      	doc add ("description" -> "")
      })

}
```

Note that the `FormatterWithEvolution` adds a `_version` field to your `BSON` document.

Helpers
=======

The `BSONDocumentHelpers` adds an implicit conversion that adds extra methods to the `BSONDocument` type. A few examples:

``` scala
import BSONDocumentHelpers._

// remove a single key
doc remove "key"

// remove a multiple keys
doc remove ("key1", "key2")

// rename a single key
doc rename ("oldKey" -> "newKey")

// rename a multiple keys
doc rename ("oldKey1" -> "newKey1", "oldKey2" -> "newKey2")

// rename a non existing key
try {
  doc rename ("nonExisting" -> "key")
} catch {
  case KeyNotFoundException(key) => //...
}

// easy access to 'required' fields
val value = doc[String]("key")

// access to non existing key
val value = 
  try {
    doc[String]("key")
  } catch {
    case KeyNotFoundException(key) => //...
  }
```

More examples can be found in the `test` folder. 
