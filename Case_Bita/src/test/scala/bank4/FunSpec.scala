package bank4

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.bita.{ RandomScheduleHelper, Scheduler }
import akka.bita.pattern.Patterns._
import akka.util.duration._
import akka.util.Timeout
import akka.dispatch.Await

import bita.util.FileHelper
//import bita.util.{ FileHelper, TestHelper }

import bita.criteria._
import bita.ScheduleOptimization._
import org.scalatest._

import java.util.concurrent.TimeUnit

import akka.testkit.CallingThreadDispatcher
import java.util.concurrent.TimeoutException
import com.typesafe.config.ConfigFactory

class FunSpec extends FunSuite with TestHelper2 {

    // feel free to change these parameters to test the bank with various configurations.
    def name = "bank4"

    implicit val timeout = Timeout(5000, TimeUnit.MILLISECONDS)

    // delay between start and end message
    def delay = 1000

    // Available criterions in Bita: PRCriterion, PCRCriterion, PMHRCriterion 
    val criteria = Array[Criterion](PCRCriterion)

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
        RandomScheduleHelper.setSystem(system)

        // A bank without delay between messages and using CallingThreadDispatcher.
        var bankActor = system.actorOf(Bank(delay).withDispatcher(CallingThreadDispatcher.Id), "Bank")

        bankActor ! Start // Start the simulation

        try {
            println(Console.CYAN + Console.BOLD+"**ASKING**"+Console.RESET)
            val future = ask(bankActor, RegisterSender)
            val result = Await.result(future, timeout.duration).asInstanceOf[Int]
            println(Console.CYAN + Console.BOLD+"**ASKED**"+Console.RESET)

            if (result > 0) {
                bugDetected = false
                println(Console.CYAN + Console.BOLD+"**SUCCESS** Charlie has %d on his account".format(result) + Console.RESET)
            } else {
                bugDetected = true
                println(Console.CYAN + Console.BOLD+"**FAILURE** Charlie has %d on his account".format(result) + Console.RESET)
            }
        } catch {
            case e: TimeoutException => {
                bugDetected = true
                println(Console.RED + Console.BOLD+"**FAILURE** Timeout"+Console.RESET)
            }
        }
    }
}