package voters

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

import java.util.concurrent.TimeUnit

import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory

import util._

class VoterSpec extends FunSuite with ImprovedTestHelper {

    // feel free to change these parameters to test the bank with various configurations.
    def name = "voters"

    implicit val timeout = Timeout(2000, TimeUnit.MILLISECONDS)

    // delay between start and end message
    def delay = 1000

    // Available criterions in Bita: PRCriterion, PCRCriterion, PMHRCriterion 
    val criteria = Array[Criterion](PRCriterion, PCRCriterion, PMHRCriterion)

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

    test("Generate and test schedules with criterion") {
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
        system = ActorSystem("ActorSystem")
        /*system = ActorSystem("ActorSystem", ConfigFactory.parseString("""
            akka {   
                loglevel = DEBUG
                stdout-loglevel = DEBUG

                remote {
                    log-received-messages = on
                }

                actor {
                    default-dispatcher {
                        throughput = 5
                    }

                    debug {
                        receive = on
                        lifecycle = on
                        event-stream = on
                    }
                }

                event-handlers = ["akka.testkit.TestEventListener"]
            }
        """))*/
        RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
        RandomScheduleHelper.setSystem(system)

        try {
            val probe = new TestProbe(system) // Use a testprobe to represent the tests.

            val ballot = system.actorOf(Ballot(), "ballot")
            val voter1 = system.actorOf(Voter(), "voter1")
            val voter2 = system.actorOf(Voter(), "voter2")

            probe.send(ballot, Start(List(voter1, voter2))) // Start the simulation

            Thread.sleep(delay)

            probe.send(ballot, Result) // Start the simulation
            //ask(ballot, Result)

            bugDetected = probe.expectMsgPF(timeout.duration, "The winner of the election") {
                case winner: ActorRef if (winner == voter2) => {
                    println(Console.GREEN + Console.BOLD+"**SUCCESS** Voter2 has won the election"+Console.RESET)
                    false
                }

                case winner: ActorRef if (winner != voter2) => {
                    println(Console.RED + Console.BOLD+"**FAILURE** Voter2 didn't win, %s won instead".format(winner.toString()) + Console.RESET)
                    true
                }

                case msg => {
                    println(Console.RED + Console.BOLD+"**FAILURE** unkown message received: %s".format(msg) + Console.RESET)
                    true
                }
            }
        } catch {
            case e: AssertionError => {
                bugDetected = true
                println(Console.RED + Console.BOLD+"**FAILURE** %s".format(e.getMessage()) + Console.RESET)
            }

            case e: TimingException => {
                bugDetected = true
                println(Console.RED + Console.BOLD+"**FAILURE** The ballot threw an exception: %s".format(e.getMessage()) + Console.RESET)
            }
        }
    }
}