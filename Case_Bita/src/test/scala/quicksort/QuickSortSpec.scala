package quicksort

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.bita.RandomScheduleHelper
import akka.bita.pattern.Patterns._
import akka.util.duration._
import org.scalatest._
import akka.testkit.TestProbe
import akka.dispatch.Await

import util.BitaTests

class QuickSortSpec extends BitaTests {

    // The name of this test battery
    override def name = "quicksort"

    // The input to sort
    var input1: Array[Int] = Array[Int](12, 30, 11, 40, 78, 20, 10, 13)
    var input2: Array[Int] = Array[Int](43, 16, 78, 3, 47, 74, 88, 65)

    def isSorted(result: Array[Int], input: Array[Int]): Boolean = {
        //check if the result if sorted array of input
        if (input.size == result.size) {
            if (result.size > 0) {
                var inputListSorted = input.toList.sortWith((e1, e2) => (e1 < e2))

                for (i <- 0 to result.size - 1) {
                    if (result(i) != inputListSorted(i))
                        return false
                }
            }
            return true
        } else {
            return false
        }
    }

    def run {
        try { 
            system = ActorSystem("ActorSystem")
            if (random) {
                RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
                RandomScheduleHelper.setSystem(system)
            }

            var qsort = system.actorOf(Props(new QuickSort()))

            var result1 = Await.result(ask(qsort, Sort(input1)), timeout.duration)
            // var result2 = Await.result(ask(qsort, Sort(input2)), timeout.duration)

            result1 match {
                case Result(result) => {
                    if (isSorted(result, input1)) {
                        println(Console.GREEN + Console.BOLD+"Result is sorted"+Console.RESET)
                        bugDetected = false
                    } else {
                        println(Console.RED + Console.BOLD+"Result is NOT sorted"+Console.RESET)
                        bugDetected = true
                    }
                    //assert(isSorted(result, input1)) // Don't use assert here, it cause it crash
                }

                case msg => {
                    println(Console.RED + Console.BOLD+"Unknown message received: %s".format(msg) + Console.RESET)
                    bugDetected = true
                }
            }

            // result2 match {
            //     case Result(result) => {
            //         assert(isSorted(result, input2))
            //     }
            // }
        } catch {
            case e: java.util.concurrent.TimeoutException => {
                bugDetected = false
                println(Console.YELLOW + Console.BOLD+"**WARNING** %s".format(e.getMessage()) + Console.RESET)
            } 
        }
    }
}