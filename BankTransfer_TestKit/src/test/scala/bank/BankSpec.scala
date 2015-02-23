package bank

import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.actor.Props
import akka.actor.ActorSystem

import akka.util.duration._

import akka.testkit.EventFilter
import akka.testkit.{TestKit, ImplicitSender}

import org.scalatest.FunSuiteLike
import org.scalatest.junit.JUnitRunner
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.BeforeAndAfterAll
import akka.testkit.CallingThreadDispatcher

class BankSpec(_system: ActorSystem) extends TestKit(_system) 
    with ImplicitSender
    with FunSuiteLike 
    with ShouldMatchers 
    with BeforeAndAfter
    with BeforeAndAfterAll {

    //#####################################################################################################

    def this() = this(ActorSystem("MySpec", ConfigFactory.parseString("""
            akka.event-handlers = ["akka.testkit.TestEventListener"]
    """)))
  
    before {

    }
    
    after {
        
    }
    
    override def afterAll {
        system.shutdown()
    }
    
    //#####################################################################################################
    
    test("Correct balance") {
        var bankActor = system.actorOf(Bank().withDispatcher(CallingThreadDispatcher.Id))

        bankActor ! Start // Start the simulation

        within(1.seconds) {
            bankActor ! Balance
            expectMsg(500)
        }
    }

    //@TODO: probes gebruiken om de volgorde van berichten te veranderen, maar met een andere testcase
    //@TODO: dispatcher bekijken wat die oplevert (CallingThreadDispatcher)
}