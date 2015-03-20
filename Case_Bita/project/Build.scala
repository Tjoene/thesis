/**
 * Build script for test project.
 * 
 * @author Jeroen Behaegel (jeroen.behaegel@student.kuleuven.be)
 */

import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

 // Build and Project settings
object BuildSettings {
    val projectName       = "Bita"
    val buildOrg          = "org.kuleuven"
    val buildOrgName      = "KU Leuven, EP"
    val buildOrgURL       = "http://ep.khbo.be/"
    val buildDesc         = "Testing the case(s) with Bita."
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
    val sonatype = "OSS Sonatype" at "https://oss.sonatype.org/content/repositories/snapshots/"
    val maven    = "Maven" at "https://repo1.maven.org/maven2/"
    
    val myResolvers = Seq(typesafe, akka, sonatype, maven)
}

// The dependencies that are required for the project
object Dependencies {
    val bita      = "cs.edu.uiuc" %% "bita" % "0.1"
    val actor     = "com.typesafe.akka" % "akka-actor" % "2.0.3"
    val testkit   = "com.typesafe.akka" % "akka-testkit" % "2.0.3"
    val scalatest = "org.scalatest" %% "scalatest" % "2.0.M5b" % "test"

    val myDepencencies = Seq(bita, actor, testkit, scalatest)
}

// The configuration for auto formatting when you compile the files
object Formatting {
    val formatSettings = SbtScalariform.scalariformSettings ++ Seq(
        ScalariformKeys.preferences in Compile := formattingPreferences,
        ScalariformKeys.preferences in Test    := formattingPreferences
    )

    def formattingPreferences = {
        import scalariform.formatter.preferences._

        // Settings can be found here: 
        // https://github.com/mdr/scalariform/wiki/Command-line-tool#option-summary
        FormattingPreferences()
            .setPreference(AlignParameters, true)
            .setPreference(AlignSingleLineCaseStatements, true)
            .setPreference(CompactStringConcatenation, true)
            .setPreference(DoubleIndentClassDeclaration, true)
            .setPreference(FormatXml, true)
            .setPreference(IndentLocalDefs, true)
            .setPreference(IndentPackageBlocks, true)
            .setPreference(PreserveDanglingCloseParenthesis, true)
            .setPreference(PreserveSpaceBeforeArguments, true)
            .setPreference(RewriteArrowSymbols, false)
            .setPreference(SpaceBeforeColon, false)
            .setPreference(SpaceInsideBrackets, false)
            .setPreference(SpaceInsideParentheses, false)
            .setPreference(IndentSpaces, 4)
    }
}

// The actual build script, nothing should be changed in here
object BuildScript extends Build {
    import Resolvers._
    import Dependencies._
    import BuildSettings._
    import Formatting._

    lazy val proj = Project (
        id = BuildSettings.projectName,
        base = file ("."),
        settings = buildSettings ++ formatSettings ++ Seq(
            resolvers := myResolvers,
            libraryDependencies ++= myDepencencies,

            // Execute tests in the current project serially
            parallelExecution in Test := false,

            // Run the tests in a seperated JVM then the one SBT is using
            fork in Test := true,
            
            // Pass options to ScalaTest.
            testOptions in Test += Tests.Argument(
                TestFrameworks.ScalaTest, 
                "-oD", // Show the duration of a test. Add an F here to print the full stacktrace 
                "-Dverbose=1", // This is a custom variable that is passed to the test. 
                               // These should be either 0, 1 or 3. With 0 printing no extra into and 3 all the info.
                               //   0 = No extra information, only the output of the program
                               //   1 = Make the end result of the test (success or failure) stand out
                               //   2 = Give a summery of the shedules that failed
                               //   3 = Give the Measurement of all the shedules
                
                "-DrandomTime=0", // This is the time that the random sheduler needs to run before timing out.
                                  // It will also stop as soon as a shedule with a bug has been found.
                                  // When this is zero, the random sheduler isn't used in the benchmark
                                  
                "-DrandomTraces=50" // The number of random traces that needs to be generated. Bita will base it's shedules on these.
            ),

            // append several options to the list of options passed to the Java compiler
            javacOptions ++= Seq(
                "-source", BuildSettings.buildJavaVersion,
                "-target", BuildSettings.buildJavaVersion,
                "-encoding", "UTF-8"
            ),

            // append several options  to the list of options passed to the Scala compiler
            scalacOptions ++= Seq(
                "-deprecation", 
                "-explaintypes", 
                "-encoding", "UTF8", 
                "–optimise"
            )
        )
    )
}