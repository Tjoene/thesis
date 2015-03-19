package pi

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import org.scalatest._
import akka.util.duration._

class ListenerSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
        with FunSuite with BeforeAndAfterAll {

    def this() = this(ActorSystem("PiSpec"))

    override def afterAll {
        system.shutdown()
    }

    test("A Listener actor must shutdown the system") {
        val listener = system.actorOf(Listener.props)
        listener ! PiApproximation(3.14159, System.currentTimeMillis().millis)
        expectNoMsg()
    }
}
