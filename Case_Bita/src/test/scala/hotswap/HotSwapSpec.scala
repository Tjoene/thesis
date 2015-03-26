package hotswap

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.bita.RandomScheduleHelper
import akka.bita.pattern.Patterns._
import akka.util.duration._
import org.scalatest._
import akka.testkit.TestProbe

import util.BitaTests

class HotSwapSpec extends BitaTests {

    // The name of this test battery
    override def name = "hotswap"

    // This will hold the actor/testcase/application under test
    def run {
        system = ActorSystem("ActorSystem")
        if (random) {
            RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
            RandomScheduleHelper.setSystem(system)
        }

        try {
            val probe = new TestProbe(system) // Use a testprobe to represent the tests.
            var hotswap = system.actorOf(HotSwap(), "HotSwap")

            probe.send(hotswap, Foo)
            probe.send(hotswap, Bar)

            bugDetected = probe.expectMsgPF(timeout.duration, "The reaction of the HotSwap actor") {
                case msg: String if (msg == "I am already angry?") => {
                    println(Console.GREEN + Console.BOLD+"**SUCCESS** Angry, He is."+Console.RESET)
                    false
                }

                case msg: String if (msg == "I am already happy :-)") => {
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
