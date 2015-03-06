import AssemblyKeys._ 

assemblySettings

/** Project */
name := "signal-collect-core"

version := "2.0.0-SNAPSHOT"

organization := "com.signalcollect"

scalaVersion := "2.9.2"

parallelExecution in Test := false

resolvers += "Typesafe Snapshot Repository" at "http://repo.typesafe.com/typesafe/snapshots/"

/** Dependencies */
libraryDependencies ++= Seq(
 "com.typesafe.akka" % "akka-actor" % "2.0.3" ,
 "com.typesafe.akka" % "akka-remote" % "2.0.3" ,
 "org.scala-lang" % "scala-library" % "2.0.3" % "compile",
 "com.esotericsoftware.kryo" % "kryo" % "2.21-SNAPSHOT" % "compile", 
 "ch.ethz.ganymed" % "ganymed-ssh2" % "build210"  % "compile",
 "commons-codec" % "commons-codec" % "1.7"  % "compile",
 "junit" % "junit" % "4.8.2"  % "test",
 "org.specs2" %% "specs2" % "1.12.1"  % "test",
 "org.mockito" % "mockito-all" % "1.9.0"  % "test"
  )