package bita

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.bita.{ RandomScheduleHelper, Scheduler }
import akka.bita.pattern.Patterns._
import akka.util.duration._
import akka.util.Timeout
import akka.dispatch.Await
import bita.util.FileHelper
import bita.criteria._
import bita.ScheduleOptimization._
import org.scalatest._
import java.util.concurrent.TimeUnit
import akka.testkit.TestProbe
import util._
import java.io.File
import scala.io.Source

abstract class BitaTests extends FunSuite with ImprovedTestHelper with BeforeAndAfterEach {

    // The name of this test battery
    def name = "unkown"

    // Are we expecting certain shedules to fail?
    def expectFailures = false

    // The delay to wait Futures/Awaits/...
    implicit val timeout = Timeout(5000, TimeUnit.MILLISECONDS)

    // delay between start and end message
    def delay = 1000

    // Available criterions in Bita: PRCriterion, PCRCriterion, PMHRCriterion 
    def criteria = Array[Criterion](PRCriterion, PCRCriterion, PMHRCriterion)

    // Folders where we need to store the test results
    val resultDir = "test-results/%s/".format(this.name)
    val randomTracesDir = resultDir+"random/"
    val randomTracesTestDir = resultDir+"random-test/"

    var verbose = 0
    var randomTime = 0
    var randomTraces = 1

    // This test will keep on generating random schedules for 5 minutes or until an bug is found. 
    test("Test with random sheduler within a timeout", Tag("random-schedule")) {
        random = true
        if (randomTime > 0) {
            testRandomByTime(name, randomTracesTestDir, randomTime)
        }
        random = false
    }

    // Generate and test schedules at once.
    test("Generate and test schedules with criterion", Tag("test")) {
        var randomTrace = FileHelper.getFiles(randomTracesDir, (name => name.contains("-trace.txt")))
        for (criterion <- criteria) {
            for (opt <- criterion.optimizations.-(NONE)) {
                var scheduleDir = resultDir+"%s-%s/".format(criterion.name, opt)

                FileHelper.emptyDir(scheduleDir)
                runGenerateSchedulesAndTest(name, scheduleDir, randomTraces, criterion, opt)
            }
        }

        measure();
        summary();
        validate();
    }

    // This will count how many bugs there were found with a certain schedule.
    // Giving you an indication of how good a shedule is.
    private def measure() = {
        if (verbose >= 3) {
            // The number of traces after which the coverage should be measured.
            var interval = 5
            for (criterion <- criteria) {
                for (opt <- criterion.optimizations.-(NONE)) {
                    var scheduleDir = resultDir+"%s-%s/".format(criterion.name, opt)

                    if (new java.io.File(scheduleDir).exists) {
                        var randomTraces = FileHelper.getFiles(randomTracesDir, (name => name.contains("-trace.txt")))
                        FileHelper.copyFiles(randomTraces, scheduleDir)

                        var resultFile = scheduleDir+"%s-%s-result.txt".format(criterion.name, opt)
                        var traceFiles = FileHelper.getFiles(scheduleDir, (name => name.contains("-trace.txt")))
                        traceFiles = FileHelper.sortTracesByName(traceFiles, "-%s-")
                        criterion.measureCoverage(traceFiles, resultFile, interval)
                    }
                }
            }
        }
    }

    // Give a summary of where the bugs 
    // This is tool dependendant information
    private def summary() = {
        if (verbose >= 2) {
            for (path <- new File(resultDir).listFiles if path.isDirectory()) { // Iterate over all directories
                val file: File = new File(path+"\\time-bug-report.txt")
                val faulty = Source.fromFile(file).getLines().size

                if (file.isFile()) { // Check if they contain a bug report file from Bita
                    if (faulty <= 4) { // Check if the shedule was faulty shedules (should be more then 4 lines then)
                        print(Console.GREEN)
                    } else {
                        print(Console.RED)
                    }
                    Source.fromFile(file).getLines().foreach { // Iterate over the content and print it
                        println
                    }
                    println(Console.RESET)
                }
            }
        }
    }

    // This will validate if we have found a valid race condition.
    private def validate() = {
        var msg = ""

        if (verbose >= 1) {
            if (numShedules != 0) {
                if (expectFailures) {
                    if ((numFaulty == 0)) { // Show the info
                        print(Console.RED + Console.BOLD)
                        msg = "**FAILURE** Generated %d shedules and %d of them failed (Failures expected).".format(numShedules, numFaulty)
                    } else {
                        print(Console.GREEN + Console.BOLD)
                        msg = "**SUCCESS** Generated %d shedules and %d of them failed.".format(numShedules, numFaulty)
                    }
                } else {
                    if ((numFaulty == 0)) { // Show the info
                        print(Console.GREEN + Console.BOLD)
                        msg = "**SUCCESS** Generated %d shedules and %d of them failed.".format(numShedules, numFaulty)
                    } else {
                        print(Console.RED + Console.BOLD)
                        msg = "**FAILURE** Generated %d shedules and %d of them failed (No failures expected).".format(numShedules, numFaulty)
                    }
                }
            } else {
                print(Console.RED + Console.BOLD)
                msg = "**FAILURE** Something went wrong, generated %d shedules".format(numShedules, numFaulty)
            }

            println("*===========================================================================================*")
            println("|                                                                                           |")
            println("|  "+msg.padTo(87, ' ')+"  |")
            println("|                                                                                           |")
            println("*===========================================================================================*"+Console.RESET)
        }

        // Assert to make the test fail or succeed, for showing it in the testrunner
        assert(numShedules != 0, "Generated %d shedules.".format(numShedules))
        assert((numFaulty != 0) == expectFailures, msg)
    }

    override def beforeEach(td: TestData) {
        val config: Map[String, Any] = td.configMap
        verbose = config.getOrElse("verbose", "0").asInstanceOf[String].toInt // read out the config passed via scalatest options 
        randomTime = config.getOrElse("randomTime", "0").asInstanceOf[String].toInt
        randomTraces = config.getOrElse("randomTraces", "1").asInstanceOf[String].toInt
    }
}