package voters

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.bita.{ RandomScheduleHelper, Scheduler }
import akka.bita.pattern.Patterns._
import akka.util.duration._
import akka.util.Timeout
import akka.dispatch.Await
import org.scalatest._
import akka.testkit.TestProbe

import util.BitaTests

class VoterSpec extends BitaTests {

    // The name of this test battery
    override def name = "voters"

    // Are we expecting certain shedules to fail?
    override def expectFailures = true

    // This will hold the actor/testcase/application under test
    def run {
        system = ActorSystem("ActorSystem")
        if (random) {
            RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
            RandomScheduleHelper.setSystem(system)
        }

        try {
            val probe = new TestProbe(system) // Use a testprobe to represent the tests.

            val ballot = system.actorOf(Ballot(), "ballot") // create the actors
            val voter1 = system.actorOf(Voter(), "voter1")
            val voter2 = system.actorOf(Voter(), "voter2")

            probe.send(ballot, Start(List(voter1, voter2))) // Start the election

            Thread.sleep(delay)

            probe.send(ballot, Result) // Ask the result of the election

            val result = probe.expectMsgType[ActorRef](timeout.duration)
            if (result == voter2) {
                println(Console.GREEN + Console.BOLD+"**SUCCESS** Voter2 has won the election"+Console.RESET)
                bugDetected = false
            } else {
                println(Console.RED + Console.BOLD+"**FAILURE** Voter2 didn't win, %s won instead".format(result) + Console.RESET)
                bugDetected = true
            }
        } catch {
            case e: AssertionError => {
                bugDetected = false
                println(Console.YELLOW + Console.BOLD+"**WARNING** %s".format(e.getMessage()) + Console.RESET)
            }

            case e: TimingException => {
                bugDetected = true
                println(Console.RED + Console.BOLD+"**FAILURE** The ballot threw an exception: %s".format(e.getMessage()) + Console.RESET)
            }
        }
    }
}