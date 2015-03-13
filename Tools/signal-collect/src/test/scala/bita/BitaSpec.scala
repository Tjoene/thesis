package bita

import com.signalcollect._
import com.signalcollect.interfaces._
import com.signalcollect.examples.{ PageRankVertex, PageRankEdge, SudokuCell }
import com.signalcollect.configuration.{ ExecutionMode, TerminationReason }


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


class BitaSpec extends FunSuite with TestHelper {

    // feel free to change these parameters to test the bank with various configurations.
    def name = "SignalCollect"

    implicit val timeout = Timeout(5000.millisecond)

    // Available criterions in Bita: PRCriterion, PCRCriterion, PMHRCriterion 
    val criteria = Array[Criterion](PRCriterion, PCRCriterion, PMHRCriterion)

    // folders where we need to store the test results
    var allTracesDir = "test-results/%s/".format(this.name)
    var randomTracesDir = allTracesDir+"random/"
    var randomTracesTestDir = allTracesDir+"random-test/"

    // This test will keep on generating random schedules for 2,5 minutes until an bug is trigger. 
    test("Test randomly within a timeout") {
        testRandomByTime(name, randomTracesTestDir, 150) // 150 sec timeout
    }

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

    def createCircleGraph(vertices: Int): Graph = {
        val graph = GraphBuilder.build
        val idSet = (1 to vertices).toSet
        for (id <- idSet) {
            graph.addVertex(new PageRankVertex(id))
        }
        for (id <- idSet) {
            graph.addEdge(id, new PageRankEdge((id % vertices) + 1))
        }
        graph
    }

    def run {
        system = ActorSystem("ActorSystem")
        RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
        RandomScheduleHelper.setSystem(system)

        val graph = createCircleGraph(30)
        val terminationCondition = new GlobalTerminationCondition(new SumOfStates[Double], 1) {
            def shouldTerminate(sum: Option[Double]): Boolean = {
                sum.isDefined && sum.get > 20.0 && sum.get < 29.0
            }
        }
        val execConfig = ExecutionConfiguration
            .withSignalThreshold(0)
            .withGlobalTerminationCondition(terminationCondition)
            .withExecutionMode(ExecutionMode.Synchronous)
        val info = graph.execute(execConfig)
        val state = graph.forVertexWithId(1, (v: PageRankVertex) => v.state)
        val aggregate = graph.aggregate(new SumOfStates[Double]).get
        
        if (aggregate > 20.0 && aggregate < 29.0 && info.executionStatistics.terminationReason == TerminationReason.GlobalConstraintMet) {
            bugDetected = false
            println(Console.GREEN + Console.BOLD+"Wow. You didn't fail." + Console.RESET)
        } else {
            bugDetected = true
            println(Console.RED + Console.BOLD+"You failed, not big surprise" + Console.RESET)
        }
    }
}