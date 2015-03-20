package bita

import pds._

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
import akka.testkit._
import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import LogicLevel._

/**
 * Ported from pds.SimulatorTest
 * Test: AND gate propagates signal
 */
class AndPropagateSpec extends Tests {

    override def name = "PDS"

    var probe: TestProbe = _

    var cl: ActorRef = _
    var testCount = 0

    private def ticktock(time: Int): Unit = {
        probe.expectMsg(Tick(time))
        cl ! Tock(time)
    }

    private def start: Unit = {
        cl ! Start(1)
        probe.expectMsg(Start)
    }

    def run {
        system = ActorSystem("System", ConfigFactory.parseString(
            """akka.loglevel = WARNING
            akka.stdout-loglevel = INFO
            akka.actor.default-dispatcher.throughput = 1    
            akka.actor.debug.receive = on
            akka.actor.debug.lifecycle = off
            akka.actor.debug.event-stream = on
            """))

        RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
        RandomScheduleHelper.setSystem(system)

        try {
            testCount += 1
            cl = system.actorOf(Clock.props, "clock_"+testCount)

            probe = new TestProbe(system) // Use a testprobe to represent the tests.

            val in1 = system.actorOf(Wire.props("in1", Low, cl), "in1")
            val in2 = system.actorOf(Wire.props("in1", High, cl), "in2")
            val out = system.actorOf(Wire.props("out", X, cl), "out")
            probe.send(out, AddObserver(probe.ref))
            val andgate = system.actorOf(AndGate.props("and", in1, in2, out, cl))
            probe.send(cl, Start(4))
            probe.expectMsg((SignalChanged(out, X), 1))
            probe.expectMsg((SignalChanged(out, Low), 5))
            probe.expectMsg(StoppedAt(6))

            println(Console.GREEN + Console.BOLD+"**SUCCESS**"+Console.RESET)
        } catch {
            case e: AssertionError => {
                bugDetected = true
                println(Console.RED + Console.BOLD+"**FAILURE** %s".format(e.getMessage()) + Console.RESET)
            }
        }
    }
}