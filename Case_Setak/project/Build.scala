import sbt._
import sbt.Keys._

/**
 * Build script for test project.
 * 
 * @author Jeroen Behaegel (jeroen.behaegel@student.kuleuven.be)
 */

 // Build and Project settings
object BuildSettings {
    val projectName       = "Setak"
    val buildOrg          = "org.kuleuven"
    val buildOrgName      = "KU Leuven, EP"
    val buildOrgURL       = "http://ep.khbo.be/"
    val buildDesc         = "Testing the case(s) with Setak."
    val buildVersion      = "1.0"
    val buildScalaVersion = "2.9.2"
    val buildJavaVersion  = "1.7"

    val buildSettings = Seq(
        organization         := buildOrg,
        organizationName     := buildOrgName,
        organizationHomepage := Some(url(buildOrgURL)),
        description          := buildDesc,
        version              := buildVersion,
        scalaVersion         := buildScalaVersion,
        shellPrompt          := ShellPrompt.buildShellPrompt
    )
}

// Alters the shell prompt to show the current project and build version
object ShellPrompt {
    object devnull extends ProcessLogger {
        def info(s: => String) {}
        def error(s: => String) {}
        def buffer[T](f: => T): T = f
    }

    val buildShellPrompt = { 
        (state: State) => {
            val currProject = Project.extract(state).currentProject.id
            "%s (%s)> ".format(currProject, BuildSettings.buildVersion)
        }
    }
}

// Resolvers for looking up dependencies
object Resolvers {
    val typesafe = "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
    val akka     = "Akka Repo" at "http://repo.akka.io/releases/"
      
    val myResolvers = Seq(typesafe, akka)
}

// The dependencies that are needed for the project
object Dependencies {
    val setak = "edu.illinois" %% "setak" % "1.0-SNAPSHOT"

    val deps = Seq(setak)
}

// The actual build script, nothing should be changed in here
object BuildScript extends Build {
    import Resolvers._
    import Dependencies._
    import BuildSettings._

    lazy val proj = Project (
        id = BuildSettings.projectName,
        base = file ("."),
        settings = buildSettings ++ Seq(
            resolvers := myResolvers ,
            libraryDependencies ++= myDepencencies,

            // Execute tests in the current project serially
            parallelExecution in Test := false,

            // append several options to the list of options passed to the Java compiler
            javacOptions ++= Seq("-source", BuildSettings.buildJavaVersion, "-target", BuildSettings.buildJavaVersion),

            // append -deprecation to the options passed to the Scala compiler
            scalacOptions += "-deprecation"
        )
    )
}