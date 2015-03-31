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
 * Ported from com.signalcollect.GraphModificationSpec
 */
class GraphModificationSpec extends BitaTests {

    override def name = "SignalCollect-graphmod"

    def run {
        system = ActorSystem("ActorSystem")
        if (random) {
            RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
            RandomScheduleHelper.setSystem(system)
        }
        
        val g = GraphBuilder.build
        g.addVertex(new GraphModificationVertex(0, 1))
        g.addVertex(new GraphModificationVertex(1, 1))
        g.addVertex(new GraphModificationVertex(2, 1))
        g.addVertex(new GraphModificationVertex(3, 1))
        g.addEdge(0, new StateForwarderEdge(1))
        g.addEdge(1, new StateForwarderEdge(3))

        var bug = true

        var statistics = g.execute
        bug = bug && (g.aggregate(new CountVertices[GraphModificationVertex]) == 4)
        bug = bug && (statistics.aggregatedWorkerStatistics.numberOfVertices == 4)
        bug = bug && (statistics.aggregatedWorkerStatistics.verticesAdded == 4)
        bug = bug && (statistics.aggregatedWorkerStatistics.verticesRemoved == 0)
        bug = bug && (statistics.aggregatedWorkerStatistics.numberOfOutgoingEdges == 2)
        bug = bug && (statistics.aggregatedWorkerStatistics.outgoingEdgesAdded == 2)
        bug = bug && (statistics.aggregatedWorkerStatistics.outgoingEdgesRemoved == 0)
        g.removeVertices(v => (v.asInstanceOf[GraphModificationVertex].id % 2 == 0))

        statistics = g.execute
        bug = bug && (g.aggregate(new CountVertices[GraphModificationVertex]) == 2)
        bug = bug && (statistics.aggregatedWorkerStatistics.numberOfVertices == 2)
        bug = bug && (statistics.aggregatedWorkerStatistics.verticesAdded == 4)
        bug = bug && (statistics.aggregatedWorkerStatistics.verticesRemoved == 2)
        bug = bug && (statistics.aggregatedWorkerStatistics.numberOfOutgoingEdges == 1)
        bug = bug && (statistics.aggregatedWorkerStatistics.outgoingEdgesAdded == 2)
        bug = bug && (statistics.aggregatedWorkerStatistics.outgoingEdgesRemoved == 1)
        
        if (!bug) {
            bugDetected = false
            println(Console.GREEN + Console.BOLD+"***SUCCESS***" + Console.RESET)
        } else {
            bugDetected = true
            println(Console.RED + Console.BOLD+"***FAILURE***" + Console.RESET)
        }
    }
}

class GraphModificationVertex(id: Int, state: Int) extends DataGraphVertex(id, state) {
    type Signal = Int
    def collect(oldState: Int, mostRecentSignals: Iterable[Int], graphEditor: GraphEditor): Int = {
        1
    }
}