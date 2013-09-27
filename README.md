ReactiveMongo evolutions
=====================================================

Provides a solution for evolutions using [ReactiveMongo](http://reactivemongo.org/)


Installation
------------

``` scala
  val appDependencies = Seq(
    "nl.rhinofly" %% "ReactiveMongo-evolutions" % "1.0.0"
  )
  
  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    resolvers += "Rhinofly Internal Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-release-local"
  )
```


Usage
-----


More examples can be found in the `test` folder. 
