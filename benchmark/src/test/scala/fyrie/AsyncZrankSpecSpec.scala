package fyrie

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
 * Ported from net.fyrie.redis.SortedSetSpec
 * Test: zrange should get the proper range
 */
class AsyncZrankSpec extends util.BitaTests {

    override def name = "Fyrie-zrank-async"
    override def expectFailures = true

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

        try {
            val probe = new TestProbe(system) // Use a testprobe to represent the tests.
            val r = new RedisClient("localhost", 6379, RedisClientConfig(connections = 1))(system)

            r.quiet.zadd("hackers", "yukihiro matsumoto", 1965) === (true)
            r.quiet.zadd("hackers", "richard stallman", 1953) === (true)
            r.quiet.zadd("hackers", "claude shannon", 1916) === (true)
            r.quiet.zadd("hackers", "linus torvalds", 1969) === (true)
            r.quiet.zadd("hackers", "alan kay", 1940) === (true)
            r.quiet.zadd("hackers", "alan turing", 1912) === (true)

            val futureZRank = r.async.zrank("hackers", "yukihiro matsumoto")
            val futureZRevRank = r.async.zrevrank("hackers", "yukihiro matsumoto")

            val result1 = Await.result(futureZRank, timeout.duration)
            val result2 = Await.result(futureZRevRank, timeout.duration)

            if (result1 == Some(4) && result2 == Some(1)) {
                bugDetected = false
                println(Console.GREEN + Console.BOLD+"***SUCCESS***"+Console.RESET)
            } else {
                bugDetected = true
                println(Console.RED + Console.BOLD+"***FAILURE***"+Console.RESET)
            }
        } catch {
            case e: AssertionError => {
                bugDetected = true
                println(Console.YELLOW + Console.BOLD+"**WARNING** %s".format(e.getMessage()) + Console.RESET)
            }

            case e: java.util.concurrent.TimeoutException => {
                bugDetected = true
                println(Console.YELLOW + Console.BOLD+"**WARNING** %s".format(e.getMessage()) + Console.RESET)
            }

            case e: net.fyrie.redis.RedisProtocolException => {
                bugDetected = true
                println(Console.YELLOW + Console.BOLD+"**WARNING** %s".format(e.getMessage()) + Console.RESET)
            }
        }
    }
}