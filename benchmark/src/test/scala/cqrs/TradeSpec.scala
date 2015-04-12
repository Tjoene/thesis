package cqrs

import net.debasishg.domain.trade.service._
import net.debasishg.domain.trade.model.TradeModel._
import net.debasishg.domain.trade.event.InMemoryEventLog

import akka.actor.{ ActorSystem, Actor, Props, ActorRef, FSM }
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
import FSM._

import util._

/**
 * Ported from net.debasishg.domain.trade.service
 * Test: trade lifecycle
 */
class TradeSpec extends BitaTests {

    override def name = "CQRS-trade"

    override def expectFailures = true

    def run {
        system = ActorSystem("System")
        if (random) {
            RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
            RandomScheduleHelper.setSystem(system)
        }

        try {
            val probe = new TestProbe(system) // Use a testprobe to represent the tests.

            val log = new InMemoryEventLog(system)
            val finalTrades = new collection.mutable.ListBuffer[Trade]

            // make trades
            val trds =
                List(
                    Trade("a-123", "google", "r-123", HongKong, 12.25, 200),
                    Trade("a-124", "ibm", "r-124", Tokyo, 22.25, 250),
                    Trade("a-125", "cisco", "r-125", NewYork, 20.25, 150),
                    Trade("a-126", "ibm", "r-127", Singapore, 22.25, 250))

            // set up listeners
            val qry = system.actorOf(Props(new TradeQueryStore))

            // do service
            trds.foreach { trd =>
                val tlc = system.actorOf(Props(new TradeLifecycle(trd, timeout.duration, Some(log))))
                tlc ! SubscribeTransitionCallBack(qry)
                tlc ! AddValueDate
                tlc ! EnrichTrade

                val future = ask(tlc, SendOutContractNote)
                finalTrades += Await.result(future, timeout.duration).asInstanceOf[Trade]
            }
            Thread.sleep(1000)

            // // get snapshot
            // import TradeSnapshot._
            // val trades = snapshot(log, system)
            // finalTrades should equal(trades)

            // // check query store
            // val f = ask(qry, QueryAllTrades)
            // val qtrades = Await.result(f, timeout.duration).asInstanceOf[List[Trade]]
            // qtrades should equal(finalTrades)

            probe.send(qry, QueryAllTrades)

            val qtrades = probe.expectMsgType[List[Trade]](timeout.duration);
            if (qtrades == finalTrades) {
                println(Console.GREEN + Console.BOLD+"**SUCCESS**"+Console.RESET)
                bugDetected = false
            } else {
                println(Console.RED + Console.BOLD+"**FAILURE**"+Console.RESET)
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