package bita

import com.gvolpe.akka.actors._

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.bita.{ RandomScheduleHelper, Scheduler }
import akka.bita.pattern.Patterns._
import akka.util.duration._
import akka.util.Timeout
import akka.dispatch.Await

import bita.util.{ FileHelper, TestHelper }

import bita.criteria._
import bita.ScheduleOptimization._
import org.scalatest._
import akka.testkit._
import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory

class ReadSpec extends BitaTests {

    override def name = "reader"

    def run {
        system = ActorSystem("System")
        if (random) {
            RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
            RandomScheduleHelper.setSystem(system)
        }
        
        try {
            val probe = new TestProbe(system) // Use a testprobe to represent the tests.

            val reader = system.actorOf(WordCounterActor.props("files/rockbands.txt"))

            probe.send(reader, StartProcessFileMsg)

            val wordcount = probe.expectMsgType[Int](timeout.duration);
            if (wordcount == 21) {
                println(Console.GREEN + Console.BOLD+"**SUCCESS**"+Console.RESET)
                bugDetected = false    
            } else {
                println(Console.RED + Console.BOLD+"**FAILURE** word count is %d".format(wordcount)+Console.RESET)
                bugDetected = true    
            }

        } catch {
            case e: AssertionError => {
                bugDetected = false
                println(Console.YELLOW + Console.BOLD+"**WARNING** %s".format(e.getMessage()) + Console.RESET)
            }

            case e: java.util.concurrent.TimeoutException => {
                bugDetected = false
                println(Console.YELLOW + Console.BOLD+"**WARNING** %s".format(e.getMessage()) + Console.RESET)
            }
        }
    }
}