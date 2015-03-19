package voters

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.bita.{ RandomScheduleHelper, Scheduler }
import akka.bita.pattern.Patterns._
import scala.concurrent.duration._
import akka.util.Timeout
import scala.concurrent.Await
import bita.util.FileHelper
import bita.criteria._
import bita.ScheduleOptimization._
import org.scalatest._
import java.util.concurrent.TimeUnit
import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import util._
import java.io.File
import scala.io.Source

class VoterSpec extends FunSuite with ImprovedTestHelper {

    // The name of this test battery
    val name = "voters"

    // Are we expecting certain shedules to fail?
    val expectFailures = true

    // The delay to wait Futures/Awaits/...
    implicit val timeout = Timeout(2000, TimeUnit.MILLISECONDS)

    // delay between start and end message
    val delay = 1000

    // Available criterions in Bita: PRCriterion, PCRCriterion, PMHRCriterion 
    val criteria = Array[Criterion](PRCriterion, PCRCriterion, PMHRCriterion)

    // Folders where we need to store the test results
    val resultDir = "test-results/%s/".format(this.name)
    val randomTracesDir = resultDir+"random/"
    val randomTracesTestDir = resultDir+"random-test/"

    // This test will keep on generating random schedules for 5 minutes or until an bug is found. 
    // test("Test with random sheduler within a timeout", Tag("random-schedule")) {
    //     testRandomByTime(name, randomTracesTestDir, 300) // 5*60 = 300 sec timeout
    // }

    // Generates a random trace which will be used for schedule generation.
    test("Generate a random trace", Tag("random")) {
        FileHelper.emptyDir(randomTracesDir)
        var traceFiles = FileHelper.getFiles(randomTracesDir, (name => name.contains("-trace.txt")))
        var traceIndex = traceFiles.length + 1
        var newTraceName = name+"-random%s-trace.txt".format(traceIndex)
        testRandom(name, randomTracesDir, 1)
    }

    // Generate and test schedules at once.
    test("Generate and test schedules with criterion", Tag("test")) {
        var randomTrace = FileHelper.getFiles(randomTracesDir, (name => name.contains("-trace.txt")))
        for (criterion <- criteria) {
            for (opt <- criterion.optimizations.-(NONE)) {
                var scheduleDir = resultDir+"%s-%s/".format(criterion.name, opt)

                FileHelper.emptyDir(scheduleDir)
                generateAndTestGeneratedSchedules(name, randomTrace, scheduleDir, criterion, opt, -1)
            }
        }
    }

    // This will count how many bugs there were found with a certain schedule.
    // Giving you an indication of how good a shedule is.
    test("Measure the coverage of testing with schedules", Tag("measure")) {
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

    // Give a summary of where the bugs 
    test("summarize results", Tag("summary")) {
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

    // This will validate if we have found a valid race condition.
    test("validate results", Tag("validate")) {
        assert((numFaulty != 0) == expectFailures, "Generated %d shedules and %d of them failed.".format(numShedules, numFaulty))
    }

    // This will hold the actor/testcase/application under test
    def run {
        system = ActorSystem("ActorSystem")
        RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
        RandomScheduleHelper.setSystem(system)

        try {
            val probe = new TestProbe(system) // Use a testprobe to represent the tests.

            val ballot = system.actorOf(Ballot(), "ballot") // create the actors
            val voter1 = system.actorOf(Voter(), "voter1")
            val voter2 = system.actorOf(Voter(), "voter2")

            probe.send(ballot, Start(List(voter1, voter2))) // Start the election

            Thread.sleep(delay)

            probe.send(ballot, Result) // Ask the result of the election

            val result = probe.expectMsgType[ActorRef](timeout.duration)
            if (result == voter2) {
                println(Console.GREEN + Console.BOLD+"**SUCCESS** Voter2 has won the election"+Console.RESET)
                bugDetected = false
            } else {
                println(Console.RED + Console.BOLD+"**FAILURE** Voter2 didn't win, %s won instead".format(result) + Console.RESET)
                bugDetected = true
            }
        } catch {
            case e: AssertionError => {
                bugDetected = false
                println(Console.YELLOW + Console.BOLD+"**WARNING** %s".format(e.getMessage()) + Console.RESET)
            }

            case e: TimingException => {
                bugDetected = true
                println(Console.RED + Console.BOLD+"**FAILURE** The ballot threw an exception: %s".format(e.getMessage()) + Console.RESET)
            }
        }
    }
}