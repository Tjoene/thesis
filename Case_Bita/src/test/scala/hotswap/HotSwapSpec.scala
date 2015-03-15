package hotswap

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
import akka.testkit.TestProbe
import java.util.concurrent.TimeUnit

class HotSwapSpec extends FunSuite with TestHelper {

    // feel free to change these parameters to test the bank with various configurations.
    def name = "hotswap"

    implicit val timeout = Timeout(500, TimeUnit.MILLISECONDS)

    // Available criterions in Bita: PRCriterion, PCRCriterion, PMHRCriterion 
    val criteria = Array[Criterion](PRCriterion, PCRCriterion, PMHRCriterion)

    // folders where we need to store the test results
    var allTracesDir = "test-results/%s/".format(this.name)
    var randomTracesDir = allTracesDir+"random/"
    var randomTracesTestDir = allTracesDir+"random-test/"

    // // This test will keep on generating random schedules for 5 min until an bug is trigger. 
    // test("Test randomly within a timeout") {
    //     testRandomByTime(name, randomTracesTestDir, 300) // 5*60 = 300 sec timeout
    // }

    // Generates a random trace which will be used for schedule generation.
    test("Generate a random trace") {
        FileHelper.emptyDir(randomTracesDir)
        var traceFiles = FileHelper.getFiles(randomTracesDir, (name => name.contains("-trace.txt")))
        var traceIndex = traceFiles.length + 1
        var newTraceName = name+"-random%s-trace.txt".format(traceIndex)
        testRandom(name, randomTracesDir, 1)
    }

    test("Generate and test schedules") {
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

    def run {
        system = ActorSystem("System")
        RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
        RandomScheduleHelper.setSystem(system)

        try {
            val probe = new TestProbe(system) // Use a testprobe to represent the tests.
            var hotswap = system.actorOf(HotSwap(), "HotSwap")

            probe.send(hotswap, Foo)
            probe.send(hotswap, Bar)

            bugDetected = probe.expectMsgPF(timeout.duration, "The reaction of the HotSwap actor") {
                case msg: String if (msg == "I am already angry?") => {
                    println(Console.GREEN + Console.BOLD+"**SUCCESS** Angry, He is."+Console.RESET)
                    false
                }

                case msg: String if (msg == "I am already happy :-)") => {
                    println(Console.RED + Console.BOLD+"**FAILURE** Angy, He is not."+Console.RESET)
                    true
                }

                case msg => {
                    println(Console.RED + Console.BOLD+"**FAILURE** unkown message received: %s".format(msg) + Console.RESET)
                    true
                }
            }
        } catch {
            case e: AssertionError => {
                bugDetected = false
                println(Console.YELLOW + Console.BOLD+"**WARNING** %s".format(e.getMessage()) + Console.RESET)
            }
        }
    }
}
