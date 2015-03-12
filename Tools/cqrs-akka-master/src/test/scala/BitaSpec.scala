package net.debasishg.domain.trade
package service

import akka.actor.{ ActorSystem, Actor, Props, ActorRef, FSM }
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
import model.TradeModel._
import event.InMemoryEventLog
import FSM._
import java.util.concurrent.TimeoutException

class BitaSpec extends FunSuite with TestHelper {

    // feel free to change these parameters to test the bank with various configurations.
    def name = "bita"

    implicit val timeout = Timeout(5, TimeUnit.SECONDS)

    // Available criterions in Bita: PRCriterion, PCRCriterion, PMHRCriterion 
    val criteria = Array[Criterion](PRCriterion, PCRCriterion, PMHRCriterion)

    // folders where we need to store the test results
    var allTracesDir = "test-results/%s/".format(this.name)
    var randomTracesDir = allTracesDir+"random/"
    var randomTracesTestDir = allTracesDir+"random-test/"

    var generatedSchedulesNum = -1

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

            val log = new InMemoryEventLog(system)
            val finalTrades = new collection.mutable.ListBuffer[Trade]

            // make trades
            val trds = 
            List(
              Trade("a-123", "google", "r-123", HongKong, 12.25, 200),
              Trade("a-124", "ibm", "r-124", Tokyo, 22.25, 250),
              Trade("a-125", "cisco", "r-125", NewYork, 20.25, 150),
              Trade("a-126", "ibm", "r-127", Singapore, 22.25, 250))

            // set up listeners
            val qry = system.actorOf(Props(new TradeQueryStore))

            // do service
            trds.foreach {trd =>
                val tlc = system.actorOf(Props(new TradeLifecycle(trd, timeout.duration, Some(log))))
                tlc ! SubscribeTransitionCallBack(qry)
                tlc ! AddValueDate
                tlc ! EnrichTrade
                val future = ask(tlc, SendOutContractNote)
                finalTrades += Await.result(future, timeout.duration).asInstanceOf[Trade]
            }
            Thread.sleep(1000)

            // // get snapshot
            // import TradeSnapshot._
            // val trades = snapshot(log, system)
            // finalTrades should equal(trades)

            // // check query store
            // val f = ask(qry, QueryAllTrades)
            // val qtrades = Await.result(f, timeout.duration).asInstanceOf[List[Trade]]
            // qtrades should equal(finalTrades)

            probe.send(qry, QueryAllTrades)



            bugDetected = probe.expectMsgPF(timeout.duration, "The reaction of the HotSwap actor") {
                case qtrades: List[Trade] if (qtrades == finalTrades) => {
                    println(Console.GREEN + Console.BOLD+"**SUCCESS** Angry, He is."+Console.RESET)
                    false
                }

                case qtrades: List[Trade] if (qtrades != finalTrades) => {
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
                bugDetected = true
                println(Console.RED + Console.BOLD+"**FAILURE** %s".format(e.getMessage()) + Console.RESET)
            }

            case e: TimeoutException => {
                bugDetected = true
                println(Console.RED + Console.BOLD+"**FAILURE** %s".format(e.getMessage()) + Console.RESET)
            }
            
        }
    }
}