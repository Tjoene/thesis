import sbt._
import sbt.Keys._

/**
 * @author Jeroen Behaegel (jeroen.behaegel@student.kuleuven.be)
 */
object SetakBuild extends Build {
    lazy val sample = Project(
        id = "setak",
        base = file("."),
        settings = Defaults.defaultSettings ++ Seq(
            organization := "edu.illinois",
            version := "1.0-SNAPSHOT",
            scalaVersion := "2.9.2", // Not compatible with Java 8!!
            crossScalaVersions := Seq("2.9.0-1", "2.9.1"),

            //resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
            resolvers += "Akka Repo" at "http://repo.akka.io/releases/",
            //libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.3",
            libraryDependencies += "org.scalatest" % "scalatest_2.9.0" % "1.6.1",
            libraryDependencies += "se.scalablesolutions.akka" % "akka" % "1.2-RC6",
            libraryDependencies += "junit" % "junit" % "4.5",

            parallelExecution in Test := false
        )
    )
}