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

import akka.testkit.CallingThreadDispatcher
import java.util.concurrent.TimeoutException
import com.typesafe.config.ConfigFactory

class VoterSpec extends FunSuite with TestHelper {

    // feel free to change these parameters to test the bank with various configurations.
    def name = "voters"

    implicit val timeout = Timeout(5000.millisecond)

    // delay between start and end message
    def delay = 500

    // Available criterions in Bita: PRCriterion, PCRCriterion, PMHRCriterion 
    val criteria = Array[Criterion](PRCriterion)

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
                var randomTraces = FileHelper.getFiles(randomTracesDir, (name => name.contains("-trace.txt")))
                FileHelper.copyFiles(randomTraces, scheduleDir)

                var resultFile = scheduleDir+"%s-%s-result.txt".format(criterion.name, opt)
                var traceFiles = FileHelper.getFiles(scheduleDir, (name => name.contains("-trace.txt")))
                traceFiles = FileHelper.sortTracesByName(traceFiles, "-%s-")
                criterion.measureCoverage(traceFiles, resultFile, interval)
            }
        }
    }

    def run {
        //system = ActorSystem("ActorSystem")
        system = ActorSystem("ActorSystem", ConfigFactory.parseString("""
            akka {   
                loglevel = WARNING
                stdout-loglevel = WARNING

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
        """))
        RandomScheduleHelper.setSystem(system)

        val ballot = system.actorOf(Props(new Ballot).withDispatcher(CallingThreadDispatcher.Id), "ballot")
        val voter1 = system.actorOf(Props(new Voter).withDispatcher(CallingThreadDispatcher.Id), "voter1")
        val voter2 = system.actorOf(Props(new Voter).withDispatcher(CallingThreadDispatcher.Id), "voter2")

        ballot ! Start(List(voter1, voter2))

        Thread.sleep(delay)

        try {
            val future = ask(ballot, Result)
            val result = Await.result(future, timeout.duration).asInstanceOf[ActorRef]

            if (result == voter2) {
                bugDetected = false
                println(Console.CYAN + Console.BOLD+"**SUCCESS** The voter2 %s has won the election".format(result.toString()) + Console.RESET)
            } else {
                bugDetected = true
                println(Console.CYAN + Console.BOLD+"**FAILURE** Voter2 didn't win the election, instead %s won".format(result.toString()) + Console.RESET)
            }
        } catch {
            case e: TimeoutException => {
                bugDetected = true
                println(Console.RED + Console.BOLD+"**FAILURE** Timeout"+Console.RESET)
            }
        }
    }
}