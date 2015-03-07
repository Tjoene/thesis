package quicksort

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.bita.{ RandomScheduleHelper, Scheduler }
import akka.bita.pattern.Patterns._
import akka.util.duration._
import akka.util.Timeout
import akka.dispatch.Await

import bita.util.{ FileHelper, TestHelper }

import bita.criteria._
import bita.ScheduleOptimization._
import org.scalatest._

import akka.testkit.CallingThreadDispatcher
import java.util.concurrent.TimeoutException
import com.typesafe.config.ConfigFactory

class QuickSortSpec extends FunSuite with TestHelper {

    // feel free to change these parameters to test the bank with various configurations.
    def name = "quicksort"

    // The input to sort
    var input1: Array[Int] = Array[Int](12, 30, 11, 40, 78, 20, 10, 13)
    var input2: Array[Int] = Array[Int](43, 16, 78, 3, 47, 74, 88, 65)

    implicit val timeout = Timeout(5000.millisecond)

    // delay between start and end message
    def delay = 0

    // Available criterions in Bita: PRCriterion, PCRCriterion, PMHRCriterion 
    val criteria = Array[Criterion](PRCriterion, PCRCriterion)

    // folders where we need to store the test results
    var allTracesDir = "test-results/%s/".format(this.name)
    var randomTracesDir = allTracesDir+"random/"
    var randomTracesTestDir = allTracesDir+"random-test/"

    var generatedSchedulesNum = -1

    // Generates a random trace which will be used for schedule generation.
    test("Generate a random trace") {
        FileHelper.emptyDir(randomTracesDir)
        var traceFiles = FileHelper.getFiles(randomTracesDir, (name => name.contains("-trace.txt")))
        var traceIndex = traceFiles.length + 1
        var newTraceName = name+"-random%s-trace.txt".format(traceIndex)
        testRandom(name, randomTracesDir, 1)
    }

    test(" Generate and test schedules with criterion") {
        var randomTrace = FileHelper.getFiles(randomTracesDir, (name => name.contains("-trace.txt")))
        for (criterion <- criteria) {
            for (opt <- criterion.optimizations.-(NONE)) {
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
        for (criterion <- criteria) {
            for (opt <- criterion.optimizations.-(NONE)) {
                var scheduleDir = allTracesDir+"%s-%s/".format(criterion.name, opt)
                var randomTraces = FileHelper.getFiles(randomTracesDir, (name => name.contains("-trace.txt")))
                FileHelper.copyFiles(randomTraces, scheduleDir)

                var resultFile = scheduleDir+"%s-%s-result.txt".format(criterion.name, opt)
                var traceFiles = FileHelper.getFiles(scheduleDir, (name => name.contains("-trace.txt")))
                traceFiles = FileHelper.sortTracesByName(traceFiles, "-%s-")
                criterion.measureCoverage(traceFiles, resultFile, interval)
            }
        }
    }

    def isSorted(result: Array[Int], input: Array[Int]): Boolean = {
        //check if the result if sorted array of input
        if (input.size == result.size) {
            if (result.size > 0) {
                var inputListSorted = input.toList.sortWith((e1, e2) => (e1 < e2))

                for (i <- 0 to result.size - 1) {
                    if (result(i) != inputListSorted(i))
                        return false
                }
            }
            return true
        } else {
            return false
        }
    }

    def run {
        system = ActorSystem("ActorSystem")
        RandomScheduleHelper.setSystem(system)

        var qsort = system.actorOf(Props(new QuickSort()))

        var result1 = Await.result(ask(qsort, Sort(input1)), timeout.duration)
        // var result2 = Await.result(ask(qsort, Sort(input2)), timeout.duration)

        result1 match {
            case Result(result) => {
                if (isSorted(result, input1)) {
                    println(Console.GREEN + Console.BOLD+"Result is sorted"+Console.RESET)
                    bugDetected = false
                } else {
                    println(Console.RED + Console.BOLD+"Result is NOT sorted"+Console.RESET)
                    bugDetected = true
                }
                //assert(isSorted(result, input1)) // Don't use assert here, it cause it crash
            }

            case msg => {
                println(Console.RED + Console.BOLD+"Unknown message received: %s".format(msg) + Console.RESET)
                bugDetected = true
            }
        }

        // result2 match {
        //     case Result(result) => {
        //         assert(isSorted(result, input2))
        //     }
        // }
    }
}