package pi

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.bita.RandomScheduleHelper
import akka.bita.pattern.Patterns._
import akka.util.duration._
import org.scalatest._
import akka.testkit.TestProbe

import util.BitaTests

class PiSpec extends BitaTests {

    // The name of this test battery
    override def name = "pi"

    // This will hold the actor/testcase/application under test
    def run {
        system = ActorSystem("ActorSystem")
        if (random) {
            RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
            RandomScheduleHelper.setSystem(system)
        }

        try {
            val probe = new TestProbe(system) // Use a testprobe to represent the tests.

            val worker = system.actorOf(Worker())

            probe.send(worker, Work(2000, 1000)) // Ask the result of the election

            val result = probe.expectMsgType[Result](timeout.duration)
            if (result == Result(1.6666664467593578E-4)) {
                println(Console.GREEN + Console.BOLD+"**SUCCESS**"+Console.RESET)
                bugDetected = false
            } else {
                println(Console.RED + Console.BOLD+"**FAILURE**"+Console.RESET)
                bugDetected = true
            }
        } catch {
            case e: AssertionError => {
                bugDetected = false
                println(Console.YELLOW + Console.BOLD+"**WARNING** %s".format(e.getMessage()) + Console.RESET)
            }
        }
    }
}