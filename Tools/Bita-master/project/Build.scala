/**
 * Build script for Bita in Scala 2.10.4 and Akka Actor 2.3.9.
 * 
 * @author Samira Tasharofi (tasharo1@illinois.edu)
 * @author Jeroen Behaegel (jeroen.behaegel@student.kuleuven.be)
 */

package bita

import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import com.typesafe.sbt.SbtAspectj.{ Aspectj, aspectjSettings, useInstrumentedClasses }
import com.typesafe.sbt.SbtAspectj.AspectjKeys.inputs

 // Build and Project settings
object BuildSettings {
    val projectName       = "bita"
    val buildOrg          = "cs.edu.uiuc"
    val buildOrgName      = "KU Leuven, EP"
    val buildOrgURL       = "http://ep.khbo.be/"
    val buildDesc         = "Testing the case(s) with Bita."
    val buildVersion      = "0.2"
    val buildScalaVersion = "2.10.4"
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
    val actor     = "com.typesafe.akka" %% "akka-actor" % "2.3.9"
    val testkit   = "com.typesafe.akka" %% "akka-testkit" % "2.3.9"
    val scalatest = "org.scalatest" %% "scalatest" % "2.2.4"

    val myDepencencies = Seq(actor, testkit, scalatest)
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
        settings = buildSettings ++ formatSettings ++ aspectjSettings ++ Seq(
            resolvers := myResolvers,
            libraryDependencies ++= myDepencencies,

            // add akka-actor as an aspectj input (find it in the update report)
            inputs in Aspectj <++= update map { report =>
                report.matching(moduleFilter(organization = "com.typesafe.akka", name = "akka-actor"))
            },

            // replace the original akka-actor jar with the instrumented classes in runtime
            fullClasspath in Test <<= useInstrumentedClasses(Test),
            fullClasspath in Runtime <<= useInstrumentedClasses(Runtime),

            // add the compiled aspects as products
            products in Compile <++= products in Aspectj,

            // Execute tests in the current project serially
            parallelExecution in Test := false,

            // Run the tests in a seperated JVM then the one SBT is using
            fork in Test := true,
            
            // Show full stack traces and durations in ScalaTest
            testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"),

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