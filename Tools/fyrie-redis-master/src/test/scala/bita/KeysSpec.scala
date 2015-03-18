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
 */
class KeysSpec extends Tests {

  override def name = "Fyrie-Keys"

  def run {
    system = ActorSystem("ActorSystem", ConfigFactory.parseString("""
        akka {
            event-handlers = ["akka.testkit.TestEventListener"]
            loglevel = "WARNING"
            actor {
                default-dispatcher {
                    core-pool-size-min = 4
                    core-pool-size-factor = 2.0
                    throughput = 10
                }
            }
        }
    """))
    RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
    RandomScheduleHelper.setSystem(system)

    val probe = new TestProbe(system) // Use a testprobe to represent the tests.
    val r = new RedisClient("localhost", 6379, RedisClientConfig(connections = 1))(system)
    r.set("anshin-1", "debasish")
    r.set("anshin-2", "maulindu")
    var result = r.sync.keys("anshin*").size

    if (result == 2) {
      bugDetected = false
      println(Console.GREEN + Console.BOLD + "***SUCCESS***" + Console.RESET)
    } else {
      bugDetected = true
      println(Console.RED + Console.BOLD + "***FAILURE***" + Console.RESET)
    }
  }
}