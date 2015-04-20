package bita

import net.fyrie.redis._

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

/**
 * Ported from net.fyrie.redis.KeysSpec
 * Test: renamenx should give
 */
class AsyncRenamenxSpec extends BitaTests {

  override def name = "Fyrie-renamenx"

  def run {
    system = ActorSystem("ActorSystem", ConfigFactory.parseString("""
        akka {
            event-handlers = ["akka.testkit.TestEventListener"]
            loglevel = "DEBUG"
            stdout-loglevel = "DEBUG"
            actor {
                default-dispatcher {
                    core-pool-size-min = 4
                    core-pool-size-factor = 2.0
                    throughput = 10
                }
            }
        }
    """))

    if (random) {
      RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
      RandomScheduleHelper.setSystem(system)
    }

    try {
      val probe = new TestProbe(system) // Use a testprobe to represent the tests.
      val r = new RedisClient("localhost", 6379, RedisClientConfig(connections = 1))(system)

      r.set("anshin-1", "debasish")
      //r.set("anshin-2", "maulindu")

      val future = r.async.renamenx("anshin-2", "anshin-2-new")
      val result = Await.result(future, timeout.duration)

      //val result2 = r.sync.renamenx("anshin-1", "anshin-2-new")

      if (result == true) {
        bugDetected = false
        println(Console.GREEN + Console.BOLD + "***SUCCESS***" + Console.RESET)
      } else {
        bugDetected = true
        println(Console.RED + Console.BOLD + "***FAILURE***" + Console.RESET)
      }
    } catch {
      case e: AssertionError ⇒ {
        bugDetected = true
        println(Console.YELLOW + Console.BOLD + "**WARNING** %s".format(e.getMessage()) + Console.RESET)
      }

      case e: java.util.concurrent.TimeoutException ⇒ {
        bugDetected = true
        println(Console.YELLOW + Console.BOLD + "**WARNING** %s".format(e.getMessage()) + Console.RESET)
      }

      case e: net.fyrie.redis.RedisProtocolException ⇒ {
        bugDetected = true
        println(Console.YELLOW + Console.BOLD + "**WARNING** %s".format(e.getMessage()) + Console.RESET)
      }
    }
  }
}