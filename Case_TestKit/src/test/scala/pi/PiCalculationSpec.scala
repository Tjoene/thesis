package pi

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, ImplicitSender }
import org.scalatest._

class PiCalculationSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
        with FunSuite with BeforeAndAfterAll {

    def this() = this(ActorSystem("PiSpec"))

    override def afterAll {
        system.shutdown()
    }

    test("A Worker actor must send back a partial Result of Pi") {
        val worker = system.actorOf(Worker.props)
        worker ! Work(2000, 1000)
        expectMsg(Result(1.6666664467593578E-4))
    }

    test("A Master actor must stop it self") {
        val listener = system.actorOf(Listener.props)
        val master = system.actorOf(Master.props(4, 1, 1, listener), "master")
        master ! Result(1.6666664467593578E-4)
        expectNoMsg()
    }
}
