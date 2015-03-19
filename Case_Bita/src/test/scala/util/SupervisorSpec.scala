package util

import akka.actor.{ ActorSystem, Actor, Props, ActorRef, PoisonPill }
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._
import akka.util.Timeout
import scala.concurrent.Await
import org.scalatest._
import akka.testkit._
import akka.pattern._

class EchoActor() extends Actor {

    def receive = {
        case msg: String => {
            sender ! msg
        }
    }
}

class SupervisorSpec(_system: ActorSystem) extends TestKit(_system) with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with ImplicitSender {

    // the time to wait for a message
    implicit val timeout = Timeout(2, TimeUnit.SECONDS)

    var supervisor: ActorRef = _

    def this() = this(ActorSystem("TestSystem"))

    before {
        supervisor = system.actorOf(Supervisor())
    }

    after {
        supervisor ! Stop
        supervisor ! PoisonPill
    }

    test("create") {
        supervisor ! CreateClass(classOf[EchoActor], "EchoActor")
        expectMsgType[ActorRef]
    }

    test("echo") {
        val getEcho = supervisor ? CreateClass(classOf[EchoActor], "EchoActor")
        val echo = Await.result(getEcho, timeout.duration).asInstanceOf[ActorRef]

        echo ! "hello world"
        expectMsg("hello world")
    }
}