package bank2

import com.typesafe.config.ConfigFactory

import akka.actor.{ ActorSystem, Props, Actor, ActorRef }

import akka.util.duration._

import akka.testkit.{ EventFilter, TestKit, ImplicitSender, CallingThreadDispatcher }

import org.scalatest.{ FunSuite, BeforeAndAfter, BeforeAndAfterAll }
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import akka.testkit.CallingThreadDispatcher

/**
 * NOTE: this test is a copy-paste of the previous bank, with the exception that there is no 50ms
 *       delay introduced in the Bank actor between Transfer and the Balance message.
 *       This is to invoke a faulty schedules, which is a Balance is asked before the Receive is
 *       arrived on the account2 actor.
 */
class BankSpec(_system: ActorSystem) extends TestKit(_system)
        with ImplicitSender
        with FunSuite
        with ShouldMatchers
        with BeforeAndAfter
        with BeforeAndAfterAll {

    //#####################################################################################################

    // Inject an ActorSystem with custom config
    def this() = this(ActorSystem("MySpec", ConfigFactory.parseString("""
            akka.loglevel = WARNING
            akka.stdout-loglevel = WARNING
            akka.actor.default-dispatcher.throughput = 5    
            akka.actor.debug.receive = on
            akka.actor.debug.lifecycle = on
            akka.actor.debug.event-stream = on
            akka.event-handlers = ["akka.testkit.TestEventListener"]
    """)))

    before {

    }

    after {

    }

    override def afterAll {
        system.shutdown() // clean up after we're done
    }

    //#####################################################################################################

    test("Wrong balance") {
        var bankActor = system.actorOf(Bank())

        bankActor ! Start // Start the simulation

        within(500.millis) {
            bankActor ! Balance
            expectMsg(0)
        }
    }
}