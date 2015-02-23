import sbt._
import sbt.Keys._

/**
 * Build script for test project.
 * 
 * @author Jeroen Behaegel (jeroen.behaegel@student.kuleuven.be)
 */
object BuildSettings {
  val projectName       = "BankTransfer_Bita"
  val buildOrg          = "org.kuleuven"
  val buildOrgName      = "KU Leuven, EP"
  val buildOrgURL       = "http://ep.khbo.be/"
  val buildDesc         = "Testing the bank transfer example with Setak.."
  val buildVersion      = "1.0"
  val buildScalaVersion = "2.9.2"
  val buildJavaVersion  = "1.7"

  val buildSettings = Seq (
    organization         := buildOrg,
    organizationName     := buildOrgName,
    organizationHomepage := Some(url(buildOrgURL)),
    description          := buildDesc,
    version              := buildVersion,
    scalaVersion         := buildScalaVersion,
    shellPrompt          := ShellPrompt.buildShellPrompt
  )
}

// Shell prompt which show the current project and build version
object ShellPrompt {
  object devnull extends ProcessLogger {
    def info (s: => String) {}
    def error (s: => String) {}
    def buffer[T] (f: => T): T = f
  }

  val buildShellPrompt = { 
    (state: State) => {
      val currProject = Project.extract (state).currentProject.id
      "%s:%s> ".format (
        currProject, BuildSettings.buildVersion
      )
    }
  }
}

object Resolvers {
  val typesaferepo = "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
  val akkerepo     = "Akka Repo" at "http://repo.akka.io/releases/"
  
  val myResolvers = Seq (typesaferepo, akkerepo)
}

object Dependencies {
  val bita      = "cs.edu.uiuc" %% "bita" % "0.1"

  val deps = Seq (bita)
}

object MyFreakingBuild extends Build {
  import Resolvers._
  import Dependencies._
  import BuildSettings._

  lazy val proj = Project (
    id = BuildSettings.projectName,
    base = file ("."),
    settings = buildSettings ++ Seq (
      resolvers := myResolvers,
      libraryDependencies ++= deps,

      // Execute tests in the current project serially
      parallelExecution in Test := false,

      // append several options to the list of options passed to the Java compiler
      javacOptions ++= Seq("-source", BuildSettings.buildJavaVersion, "-target", BuildSettings.buildJavaVersion),

      // append -deprecation to the options passed to the Scala compiler
      scalacOptions += "-deprecation"
    )
  )
}