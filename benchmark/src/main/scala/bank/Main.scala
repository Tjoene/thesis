package bank

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.dispatch.Await
import akka.util.duration._
import akka.util.Timeout
import akka.dispatch.DefaultPromise
import akka.dispatch.{ Promise, Future }
import akka.pattern.ask

object BankMain {

    implicit val timeout = Timeout(5000.millisecond)

    def main(args: Array[String]) {
        val system = ActorSystem("BankMain")

        var bankActor = system.actorOf(Bank())

        bankActor ! Start // Start the simulation

        val future = (bankActor ? RegisterSender)
        var result = Await.result(future, timeout.duration).asInstanceOf[Int]

        if (result == 500) {
            println(Console.YELLOW + Console.BOLD+"**SUCCESS** Freddy has %d on his account".format(result))
        } else {
            println(Console.YELLOW + Console.BOLD+"**FAILURE** Freddy has %d on his account".format(result))
        }

        system.shutdown()
    }
}
