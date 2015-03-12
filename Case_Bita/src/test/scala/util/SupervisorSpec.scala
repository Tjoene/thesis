package util

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import java.util.concurrent.TimeUnit
import akka.util.duration._
import akka.util.Timeout
import akka.dispatch.Await
import org.scalatest._
import akka.testkit._
import akka.pattern._

class EchoActor() extends Actor {

    def receive = {
        case msg: String => {
            println("SENDING BACK MESSAGE '%s' TO SENDER '%s'".format(msg, sender.toString()))
            sender ! msg
        }
    }
}

class SupervisorSpec(_system: ActorSystem) extends TestKit(_system) with FunSuite with BeforeAndAfter with BeforeAndAfterAll with ImplicitSender {

    // the time to wait for a message
    implicit val timeout = Timeout(2, TimeUnit.SECONDS)

    var supervisor: ActorRef = _

    def this() = this(ActorSystem("TestSystem"))

    before {
        supervisor = system.actorOf(Supervisor(), "supervisor")
    }

    after {
        supervisor ! Stop
    }

    test("echo") {
        val getEcho = supervisor ? CreateClass(classOf[EchoActor], "EchoActor")
        val echo = Await.result(getEcho, timeout.duration).asInstanceOf[ActorRef]

        println("%s".format(echo.toString()))

        echo ! "hello world"
        expectMsg("hello world")
    }
}