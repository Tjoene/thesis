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

class MoreDelayBankSpec extends BankSpec {

    // feel free to change these parameters to test the bank with various configurations.
    override val name = "bank4"
    override val delay = 1000
}
