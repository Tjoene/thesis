package bank

import com.typesafe.config.ConfigFactory

import akka.actor.{ ActorSystem, Props, Actor, ActorRef }

import akka.util.duration._

import akka.testkit.{ EventFilter, TestKit, ImplicitSender, CallingThreadDispatcher }

import org.scalatest.{ FunSuiteLike, BeforeAndAfter, BeforeAndAfterAll }
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner

class BankSpec(_system: ActorSystem) extends TestKit(_system) 
    with ImplicitSender
    with FunSuiteLike 
    with ShouldMatchers 
    with BeforeAndAfter
    with BeforeAndAfterAll {

    //#####################################################################################################

    // Inject an ActorSystem with custom config
    def this() = this(ActorSystem("MySpec", ConfigFactory.parseString("""
            akka.loglevel = WARNING
            akka.stdout-loglevel = DEBUG
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
    
    test("Correct balance") {
        var bankActor = system.actorOf(Bank().withDispatcher(CallingThreadDispatcher.Id))

        bankActor ! Start // Start the simulation

        within(500.millis) {
            bankActor ! Balance
            expectMsg(500)
        }
    }

    //@TODO: probes gebruiken om de volgorde van berichten te veranderen, maar met een andere testcase
    //@TODO: dispatcher bekijken wat die oplevert (CallingThreadDispatcher)
}