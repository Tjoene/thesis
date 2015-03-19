package bank2

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.bita.{ RandomScheduleHelper, Scheduler }
import akka.bita.pattern.Patterns._
import scala.concurrent.duration._
import akka.util.Timeout
import scala.concurrent.Await
import bita.util.{ FileHelper, ImprovedTestHelper }
import bita.criteria._
import bita.ScheduleOptimization._
import org.scalatest._
import akka.testkit.TestProbe
import java.util.concurrent.TimeUnit

class BankSpec extends FunSuite with ImprovedTestHelper {

    // feel free to change these parameters to test the bank with various configurations.
    def name = "bank2"

    implicit val timeout = Timeout(5000, TimeUnit.MILLISECONDS)

    // delay between start and end message
    def delay = 0

    // Available criterions in Bita: PRCriterion, PCRCriterion, PMHRCriterion 
    val criteria = Array[Criterion](PRCriterion, PCRCriterion, PMHRCriterion)

    // folders where we need to store the test results
    var allTracesDir = "test-results/%s/".format(this.name)
    var randomTracesDir = allTracesDir+"random/"
    var randomTracesTestDir = allTracesDir+"random-test/"

    // // This test will keep on generating random schedules for 10 seconds until an bug is trigger. 
    // test("Test randomly within a timeout") {
    //     testRandomByTime(name, randomTracesTestDir, 10) // 10 sec timeout
    // }

    // Generates a random trace which will be used for schedule generation.
    test("Generate a random trace") {
        FileHelper.emptyDir(randomTracesDir)
        var traceFiles = FileHelper.getFiles(randomTracesDir, (name => name.contains("-trace.txt")))
        var traceIndex = traceFiles.length + 1
        var newTraceName = name+"-random%s-trace.txt".format(traceIndex)
        testRandom(name, randomTracesDir, 1)
    }

    // test("Generate schedules") {
    //     var randomTrace = FileHelper.getFiles(randomTracesDir, (name => name.contains("-trace.txt")))
    //     for (opt <- criterion.optimizations.-(NONE)) {
    //         var scheduleDir = allTracesDir + "%s-%s/schedules/".format(criterion.name, opt)
    //         FileHelper.emptyDir(scheduleDir)
    //         generateSchedules(name, randomTrace, scheduleDir, criterion, opt, -1)
    //     }
    // }

    // test("Test the generated schedules") {
    //     for (opt <- criterion.optimizations.-(NONE)) {
    //         var scheduleDir = allTracesDir + "%s-%s/schedules/".format(criterion.name, opt)

    //         var traceFiles = FileHelper.getFiles(scheduleDir, (name => name.contains("-trace.txt")))
    //         var scheduleIndex = traceFiles.length + 1
    //         var newScheduleFileName = name + "-%s-schedule.txt".format(scheduleIndex)
    //         testGeneratedSchedules(scheduleDir)
    //     }
    // }

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
            var bank = system.actorOf(Bank(delay, probe.ref), "Bank") // A bank without delay between messages.

            probe.send(bank, Start) // Start the simulation

            bugDetected = probe.expectMsgPF(timeout.duration, "The amount on charlie's account") {
                case amount: Int if (amount > 0) => {
                    println(Console.GREEN + Console.BOLD+"**SUCCESS** Charlie has %d on his account".format(amount) + Console.RESET)
                    false
                }

                case amount: Int if (amount <= 0) => {
                    println(Console.RED + Console.BOLD+"**FAILURE** Charlie has %d on his account".format(amount) + Console.RESET)
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
