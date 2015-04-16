package bita

import com.redis._

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
 * Test: keys should fetch keys
 */
class InsertSpec extends BitaTests {

  override def name = "Fyrie-insert"

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

    if (random) {
      RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
      RandomScheduleHelper.setSystem(system)
    }
    
    val probe = new TestProbe(system) // Use a testprobe to represent the tests.
    val r = new RedisClient("localhost", 6379)

    r.set("anshin-1", "debasish")
    r.get("anshin-1") match {
        case Some(result: String) => {
          if (result == "debasish") {
            bugDetected = false
            println(Console.GREEN + Console.BOLD + "***SUCCESS***" + Console.RESET)
          } else {
            bugDetected = true
            println(Console.RED + Console.BOLD + "***FAILURE***" + Console.RESET)
          }
        }
        case None => {
          bugDetected = true
          println(Console.RED + Console.BOLD + "***FAILURE*** should return debasish" + Console.RESET)
        }
      }

    r.flushdb // Empty the redis server
    r.flushall
  }
}
