import sbt._
import Keys._

object FyrieRedisBuild extends Build {
  lazy val core = Project("fyrie-redis",
                          file("."),
                          settings = coreSettings)

  val coreSettings = Defaults.defaultSettings ++ Seq(
    scalaVersion := "2.9.1",
    crossScalaVersions := Seq("2.9.0-1", "2.9.1"),
    name := "fyrie-redis",
    organization := "net.fyrie",
    version := "1.2-SNAPSHOT",
    resolvers += "Akka Repo" at "http://repo.akka.io/releases/",
    libraryDependencies ++= Seq("se.scalablesolutions.akka" % "akka-actor" % "1.2" % "compile",
                                "org.specs2" % "specs2_2.9.1" % "1.6.1",
                                "org.specs2" % "specs2-scalaz-core_2.9.1" % "6.0.1" % "test"),
    parallelExecution in Test := false,
    publishTo <<= (version) { version: String =>
      val repo = (s: String) =>
        Resolver.ssh(s, "repo.fyrie.net", "/home/repo/" + s + "/") as("derek", file("/home/derek/.ssh/id_rsa")) withPermissions("0644")
      Some(if (version.trim.endsWith("SNAPSHOT")) repo("snapshots") else repo("releases"))
    })
}
