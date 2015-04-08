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

/**
 * Ported from com.signalcollect.features.ComputationTerminationSpec
 * Test: Steps limit should work for synchronous computations
 */
class BugSpec extends BitaTests {

    override def name = "SignalCollect-bug"

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

        val graph = createCircleGraph(1000)
        val execConfig = ExecutionConfiguration
            .withSignalThreshold(0)
            .withStepsLimit(1)
            .withExecutionMode(ExecutionMode.Synchronous)
        val info = graph.execute(execConfig)
        val state = graph.forVertexWithId(1, (v: PageRankVertex) => v.state)

        if (state == 0.2775 && info.executionStatistics.terminationReason == TerminationReason.ComputationStepLimitReached) {
            bugDetected = false
            println(Console.GREEN + Console.BOLD+"***SUCCESS***"+Console.RESET)
        } else {
            bugDetected = true
            println(Console.RED + Console.BOLD+"***FAILURE***"+Console.RESET)
        }
    }
}