import sbt._
import Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys

object Build extends Build {

  val libraryName = "ReactiveMongo-evolutions"
  val libraryVersion = "1.0.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.reactivemongo" %% "reactivemongo" % "0.9",
    "org.specs2" %% "specs2" % "2.2.2" % "test")

  val defaultSettings = Seq(
    version := libraryVersion,
    libraryDependencies ++= appDependencies,
    scalacOptions += "-feature",
    organization := "play.modules.mailer",
    resolvers ++= Seq(
      rhinoflyRepo("RELEASE").get,
      "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
      "releases" at "http://oss.sonatype.org/content/repositories/releases"),
    publishTo <<= version(rhinoflyRepo),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"))

  val eclipseSettings = Seq(
    EclipseKeys.withSource := true,
    unmanagedSourceDirectories in Compile <<= Seq(scalaSource in Compile).join,
    unmanagedSourceDirectories in Test <<= Seq(scalaSource in Test).join)

  def rhinoflyRepo(version: String) = {
    val repo = if (version endsWith "SNAPSHOT") "snapshot" else "release"
    Some("Rhinofly Internal " + repo.capitalize + " Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-" + repo + "-local")
  }

  lazy val root = Project(id = libraryName,
    base = file("."),
    settings =
      Project.defaultSettings ++
        defaultSettings ++
        eclipseSettings)
}