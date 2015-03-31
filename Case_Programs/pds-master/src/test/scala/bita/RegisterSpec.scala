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
 * Test: introduce a new signalchanged in the simulation
 */
class RegisterSpec extends BitaTests {

    override def name = "PDS-Register"

    var probe: TestProbe = _

    var cl: ActorRef = _

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

        if (random) {
            RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
            RandomScheduleHelper.setSystem(system)
        }
        
        try {
            probe = new TestProbe(system) // Use a testprobe to represent the tests.

            cl = system.actorOf(Clock.props, "clock")

            cl ! Start(1)

            cl ! Register

            probe.expectMsg(Start)

            ticktock(0)
            probe.expectMsg(StoppedAt(1))

            println(Console.GREEN + Console.BOLD+"**SUCCESS**"+Console.RESET)
        } catch {
            case e: AssertionError => {
                bugDetected = true
                println(Console.RED + Console.BOLD+"**FAILURE** %s".format(e.getMessage()) + Console.RESET)
            }
        }
    }
}