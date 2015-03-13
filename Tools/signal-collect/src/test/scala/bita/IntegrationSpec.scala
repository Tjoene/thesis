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
 * Ported from com.signalcollect.IntegrationSpec
 * PageRank algorithm should deliver correct results on a 5-cycle graph
 */
class IntegrationSpec extends Tests {

    override def name = "SignalCollect-integration"

    val computeGraphFactories: List[() => Graph] = List(() => GraphBuilder.build)
    val executionModes = List(ExecutionMode.Synchronous, ExecutionMode.OptimizedAsynchronous)

    def test(graphProviders: List[() => Graph] = computeGraphFactories, verify: Vertex[_, _] => Boolean, buildGraph: Graph => Unit = (graph: Graph) => (), signalThreshold: Double = 0.01, collectThreshold: Double = 0): Boolean = {
        var correct = true
        var computationStatistics = Map[String, List[ExecutionInformation]]()

        for (executionMode <- executionModes) {
            println("ExecutionMode: " + executionMode)
            for (graphProvider <- graphProviders) {
                val graph = graphProvider()
                buildGraph(graph)
                println("Graph has been built.")
                val stats = graph.execute(ExecutionConfiguration(executionMode = executionMode, signalThreshold = signalThreshold))
                correct &= graph.aggregate(new AggregationOperation[Boolean] {
                    val neutralElement = true
                    def aggregate(a: Boolean, b: Boolean): Boolean = a && b
                    def extract(v: Vertex[_, _]): Boolean = verify(v)
                })
                if (!correct) {
                    System.err.println("Test failed. Computation stats: " + stats)
                }
                println("Test completed, shutting down...")
                graph.shutdown
                println("Shutdown completed.")
            }
        }
        correct
    }

    def buildPageRankGraph(graph: Graph, edgeTuples: Traversable[Tuple2[Int, Int]]): Graph = {
        edgeTuples foreach {
            case (sourceId: Int, targetId: Int) => {
                graph.addVertex(new PageRankVertex(sourceId, 0.85))
                graph.addVertex(new PageRankVertex(targetId, 0.85))
                graph.addEdge(sourceId, new PageRankEdge(targetId))
            }
        }
        graph
    }

    def run {
        system = ActorSystem("ActorSystem")
        RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
        RandomScheduleHelper.setSystem(system)

        println("PageRank algorithm on a 5-cycle graph")
        val fiveCycleEdges = List((0, 1), (1, 2), (2, 3), (3, 4), (4, 0))
        def pageRankFiveCycleVerifier(v: Vertex[_, _]): Boolean = {
            val state = v.state.asInstanceOf[Double]
            val expectedState = 1.0
            val correct = (state - expectedState).abs < 0.001
            if (!correct) {
                System.out.println("Problematic vertex:  id=" + v.id + ", expected state=" + expectedState + ", actual state=" + state)
            }
            correct
        }

        if (test(verify = pageRankFiveCycleVerifier, buildGraph = buildPageRankGraph(_, fiveCycleEdges), signalThreshold = 0.00001)) {
            bugDetected = false
            println(Console.GREEN + Console.BOLD+"***SUCCESS***" + Console.RESET)
        } else {
            bugDetected = true
            println(Console.RED + Console.BOLD+"***FAILURE***" + Console.RESET)
        }
    }
}