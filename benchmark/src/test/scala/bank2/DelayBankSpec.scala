package bank2

import akka.bita.Scheduler
import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.bita.pattern.Patterns._
import akka.dispatch.Await
import akka.util.duration._
import akka.util.Timeout
import akka.dispatch.DefaultPromise
import akka.dispatch.{ Promise, Future }
import bita.{ ScheduleEnvelope, LogicalMessage, EventID }
import bita.util.FileHelper
import bita.criteria._
import bita.ScheduleOptimization._
import bita.util.TestHelper
import org.scalatest._
import akka.bita.RandomScheduleHelper
import akka.bita.pattern.Patterns.ask

class DelayBankSpec extends BankSpec {

    // feel free to change these parameters to test the bank with various configurations.
    override def name = "bank2_delay"
    override def delay = 500
}
