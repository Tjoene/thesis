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

abstract class Tests extends FunSuite with ImprovedTestHelper {

    // feel free to change these parameters to test the bank with various configurations.
    def name = "PDS"

    implicit val timeout = Timeout(2000.millisecond)

    // Available criterions in Bita: PRCriterion, PCRCriterion, PMHRCriterion 
    val criteria = Array[Criterion](PRCriterion, PCRCriterion, PMHRCriterion)

    // folders where we need to store the test results
    var allTracesDir = "test-results/%s/".format(this.name)
    var randomTracesDir = allTracesDir+"random/"
    var randomTracesTestDir = allTracesDir+"random-test/"

    // // This test will keep on generating random schedules for 2,5 minutes until an bug is trigger. 
    // test("Test randomly within a timeout") {
    //     testRandomByTime(name, randomTracesTestDir, 150) // 150 sec timeout
    // }

    // Generates a random trace which will be used for schedule generation.
    test("Generate a random trace") {
        FileHelper.emptyDir(randomTracesDir)
        var traceFiles = FileHelper.getFiles(randomTracesDir, (name ⇒ name.contains("-trace.txt")))
        var traceIndex = traceFiles.length + 1
        var newTraceName = name+"-random%s-trace.txt".format(traceIndex)
        testRandom(name, randomTracesDir, 1)
    }

    test(" Generate and test schedules with criterion") {
        var randomTrace = FileHelper.getFiles(randomTracesDir, (name ⇒ name.contains("-trace.txt")))
        for (criterion ← criteria) {
            for (opt ← criterion.optimizations.-(NONE)) {
                var scheduleDir = allTracesDir+"%s-%s/".format(criterion.name, opt)

                FileHelper.emptyDir(scheduleDir)
                generateAndTestGeneratedSchedules(name, randomTrace, scheduleDir, criterion, opt, -1)
            }
        }
    }

    // This will count how many bugs there were found with a certain schedule.
    // Giving you an indication of how good a shedule is.
    test("Measure the coverage of testing with schedules") {
        // The number of traces after which the coverage should be measured.
        var interval = 5
        for (criterion ← criteria) {
            for (opt ← criterion.optimizations.-(NONE)) {
                var scheduleDir = allTracesDir+"%s-%s/".format(criterion.name, opt)
                var randomTraces = FileHelper.getFiles(randomTracesDir, (name ⇒ name.contains("-trace.txt")))
                FileHelper.copyFiles(randomTraces, scheduleDir)

                var resultFile = scheduleDir+"%s-%s-result.txt".format(criterion.name, opt)
                var traceFiles = FileHelper.getFiles(scheduleDir, (name ⇒ name.contains("-trace.txt")))
                traceFiles = FileHelper.sortTracesByName(traceFiles, "-%s-")
                criterion.measureCoverage(traceFiles, resultFile, interval)
            }
        }
    }
}