package bank4

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.bita.RandomScheduleHelper
import akka.bita.pattern.Patterns._
import akka.util.duration._
import org.scalatest._
import akka.testkit.{ TestProbe, CallingThreadDispatcher }

import util.BitaTests

class BankSpec extends BitaTests {

    // feel free to change these parameters to test the bank with various configurations.
    override def name = "bank4"

    // Are we expecting certain shedules to fail?
    override def expectFailures = true

    // delay between start and end message
    override def delay = 0

    def run {
        system = ActorSystem("System")
        if (random) {
            RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
            RandomScheduleHelper.setSystem(system)
        }

        try {
            val probe = new TestProbe(system) // Use a testprobe to represent the tests.
            var bank = system.actorOf(Bank(delay).withDispatcher(CallingThreadDispatcher.Id), "Bank") // A bank without delay between messages.

            probe.send(bank, Start) // Start the simulation

            val result = probe.expectMsgType[Int](timeout.duration)
            if (result > 0) {
                println(Console.GREEN + Console.BOLD+"**SUCCESS** Charlie has %d on his account".format(result) + Console.RESET)
                bugDetected = false
            } else {
                println(Console.RED + Console.BOLD+"**FAILURE** Charlie has %d on his account".format(result) + Console.RESET)
                bugDetected = true
            }
        } catch {
            case e: AssertionError => {
                bugDetected = false
                println(Console.YELLOW + Console.BOLD+"**WARNING** %s".format(e.getMessage()) + Console.RESET)
            }

            case e: java.util.concurrent.TimeoutException => {
                bugDetected = false
                println(Console.YELLOW + Console.BOLD+"**WARNING** %s".format(e.getMessage()) + Console.RESET)
            }
        }
    }
}
