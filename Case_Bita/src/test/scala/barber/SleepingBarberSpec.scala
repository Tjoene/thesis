package barber

import scala.collection.mutable.{ HashMap, ArrayBuffer, Queue }
import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.bita.RandomScheduleHelper
import akka.bita.pattern.Patterns._
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.{ Promise, Future }
import bita.{ ScheduleEnvelope, LogicalMessage, EventID }
import bita.util.FileHelper
import bita.criteria._
import bita.ScheduleOptimization._
import bita.ScheduleOptimization
import bita.util.ImprovedTestHelper
import org.scalatest._
import akka.bita.RandomScheduleHelper
import java.util.concurrent.TimeUnit

import util.BitaTests

/**
 * @author Samira Tasharofi
 */
class BarberSpec extends BitaTests {

    // The name of this test battery
    override def name = "barber"

    // Are we expecting certain shedules to fail?
    override def expectFailures = true

    // feel free to change these parameters to test the barber with various configurations.
    var capacity = 2
    var customerNum = 3

    // This will hold the actor/testcase/application under test
    def run {
        system = ActorSystem()
        if (random) {
            RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
            RandomScheduleHelper.setSystem(system)
        }

        try {
            var promise = Promise[HashMap[CustomerState.Value, Int]]() //(system.dispatcher)

            var monitor = system.actorOf(Props(new Monitor(customerNum, promise)))
            var barber = system.actorOf(Props(new Barber))

            var waitingRoom = system.actorOf(Props(new WaitingRoom(capacity, barber)))
            var customers = new Array[ActorRef](customerNum)

            for (i <- 0 to customerNum - 1) {
                customers(i) = system.actorOf(Props(new Customer(i+"", waitingRoom, monitor)))
            }

            for (i <- 0 to customerNum - 1) {
                customers(i) ! Go
            }

            var result = Await.result(promise.future.mapTo[HashMap[CustomerState.Value, Int]], timeout.duration)

            if (result.asInstanceOf[HashMap[CustomerState.Value, Int]].contains(CustomerState.Exception)) {
                bugDetected = true
            }

            println(result)
        } catch {
            case e: java.util.concurrent.TimeoutException => {
                bugDetected = true
                println(Console.YELLOW + Console.BOLD+"**WARNING** %s".format(e.getMessage()) + Console.RESET)
            }
        }
    }
}
