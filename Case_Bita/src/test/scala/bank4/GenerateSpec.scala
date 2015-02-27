package bank4

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

class GenerateSpec extends FunSpec with Bita.util.TestHelper {

    // feel free to change these parameters to test the bank with various configurations.
    def name = "bank4"

    implicit val timeout = Timeout(5000.millisecond)

    // delay between start and end message
    def delay = 1000

    // Available criterions in Bita: PRCriterion, PCRCriterion, PMHRCriterion 
    val criteria = Array[Criterion](PRCriterion)

    // folders where we need to store the test results
    var allTracesDir = "test-results/%s/".format(this.name)
    var randomTracesDir = allTracesDir + "random/"
    var randomTracesTestDir = allTracesDir + "random-test/"

    var generatedSchedulesNum = -1

    describe("Generate Random Schedule") {
        // Generates a random trace which will be used for schedule generation.
        it(" should generate a random trace", Tag("random")) {
            FileHelper.emptyDir(randomTracesDir)
            var traceFiles = FileHelper.getFiles(randomTracesDir, (name => name.contains("-trace.txt")))
            var traceIndex = traceFiles.length + 1
            var newTraceName = name + "-random%s-trace.txt".format(traceIndex)
            testRandom(name, randomTracesDir, 1)
        }

        it(" should generate and test schedules ", Tag("generate-test")) {
            var randomTrace = FileHelper.getFiles(randomTracesDir, (name => name.contains("-trace.txt")))
            for (criterion <- criteria) {
                for (opt <- criterion.optimizations.-(NONE)) {
                    var scheduleDir = allTracesDir + "%s-%s/".format(criterion.name, opt)
                    
                    FileHelper.emptyDir(scheduleDir)
                    generateAndTestGeneratedSchedules(name, randomTrace, scheduleDir, criterion, opt, -1)
                }
            }
        }

        // This will count how many bugs there were found with a certain schedule.
        // Giving you an indication of how good a shedule is.
        it(" should measure the coverage of testing with schedules ", Tag("coverage")) {
            // The number of traces after which the coverage should be measured.
            var interval = 5
            for (criterion <- criteria) {
                for (opt <- criterion.optimizations.-(NONE)) {
                    var scheduleDir = allTracesDir + "%s-%s/".format(criterion.name, opt)
                    var randomTraces = FileHelper.getFiles(randomTracesDir, (name => name.contains("-trace.txt")))
                    FileHelper.copyFiles(randomTraces, scheduleDir)

                    var resultFile = scheduleDir + "%s-%s-result.txt".format(criterion.name, opt)
                    var traceFiles = FileHelper.getFiles(scheduleDir, (name => name.contains("-trace.txt")))
                    traceFiles = FileHelper.sortTracesByName(traceFiles, "-%s-")
                    criterion.measureCoverage(traceFiles, resultFile, interval)
                }
            }
        }
    }

    def run {
        system = ActorSystem()
        RandomScheduleHelper.setSystem(system)

        // A bank without delay between messages and using CallingThreadDispatcher.
        var bankActor = system.actorOf(Bank(delay).withDispatcher(CallingThreadDispatcher.Id), "Bank") 

        bankActor ! RegisterSender
        bankActor ! Start // Start the simulation

        val future = ask(bankActor, RegisterSender)
        val result = Await.result(future, timeout.duration).asInstanceOf[Int]
        
        if(result > 0) {
            bugDetected = false
            println(Console.YELLOW + Console.BOLD + "**SUCCESS** Charlie has %d on his account".format(result) + Console.RESET)
        } else {
            bugDetected = true
            println(Console.YELLOW + Console.BOLD + "**FAILURE** Charlie has %d on his account".format(result) + Console.RESET)
        }
    }
}

/*class QuickSortSpec2 extends FlatSpec with bita.util.TestHelper {

  implicit val timeout = Timeout(2500.millisecond)
  var random = false

  "The returned array" should "be sorted" in {
    runGenerateSchedulesAndTest("qsort", "./results-helper/pr-criterion/", 1, PRCriterion)
  }

  "The returned array" should "be sorted with a random scheduling" in {
    random = true
    // run the test with the radom scheduling 5 times.
    testRandom("qsort", "./results-helper/random-150/", 5)
    random = false
  }

  def run() {
    system = ActorSystem()
    if (random) {
      RandomScheduleHelper.setMaxDelay(150)
      RandomScheduleHelper.setSystem(system)
    }
    // A bank without delay between messages and using CallingThreadDispatcher.
        var bankActor = system.actorOf(Bank(1000).withDispatcher(CallingThreadDispatcher.Id), "Bank") 

        bankActor ! RegisterSender
        bankActor ! Start // Start the simulation

        try { 
            val future = ask(bankActor, RegisterSender)
            val result = Await.result(future, timeout.duration).asInstanceOf[Int]
            
            assert(result > 0)
        } catch {
            case e: TimeoutException => {
                println(Console.RED + Console.BOLD + "**FAILURE** Timeout" + Console.RESET)
                assert(false);
            } 
        }
  }
}*/