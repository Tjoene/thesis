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
 */
class ComputationTerminationSpec extends BitaTests {

    override def name = "SignalCollect-computation"

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
            println(Console.GREEN + Console.BOLD+"***SUCCESS***"+Console.RESET)
        } else {
            bugDetected = true
            println(Console.RED + Console.BOLD+"***FAILURE***"+Console.RESET)
        }
    }
}