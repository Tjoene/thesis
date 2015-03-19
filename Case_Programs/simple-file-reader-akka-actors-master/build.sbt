name := """simple-file-reader-akka-actors"""

version := "1.0.0"

scalaVersion := "2.9.2"

libraryDependencies ++= Seq(
  "cs.edu.uiuc" %% "bita" % "0.1",
  "com.typesafe.akka" % "akka-actor" % "2.0.3",
  "com.typesafe.akka" % "akka-testkit" % "2.0.3" % "test"
)

