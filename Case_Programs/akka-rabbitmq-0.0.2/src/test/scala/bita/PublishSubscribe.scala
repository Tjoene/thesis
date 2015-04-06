package bita

import com.thenewmotion.akka.rabbitmq._
import com.thenewmotion.akka.rabbitmq.ChannelActor.ChannelMessage
import com.rabbitmq.client._
import com.thenewmotion.akka.rabbitmq.ConnectionActor.{ Created, Create }

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

class PublishSubscribeSpec extends BitaTests {

    override def name = "reader"

    var probe: TestProbe = _
    val exchange = "amq.fanout"

    def setupPublisher(channel: Channel) {
        val queue = channel.queueDeclare().getQueue
        channel.queueBind(queue, exchange, "")
    }

    def setupSubscriber(channel: Channel) {
        val queue = channel.queueDeclare().getQueue
        channel.queueBind(queue, exchange, "")
        val consumer = new DefaultConsumer(channel) {
            override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]) {
                probe.ref ! fromBytes(body)
            }
        }
        channel.basicConsume(queue, true, consumer)
    }

    def fromBytes(x: Array[Byte]) = new String(x, "UTF-8").toLong
    def toBytes(x: Long) = x.toString.getBytes("UTF-8")

    def run {
        system = ActorSystem("System")
        if (random) {
            RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
            RandomScheduleHelper.setSystem(system)
        }

        try {
            probe = new TestProbe(system) // Use a testprobe to represent the tests.

            val factory = new ConnectionFactory()
            val connection = system.actorOf(Props(new ConnectionActor(factory)), "rabbitmq")

            connection ! Create(Props(new ChannelActor(setupPublisher)), Some("publisher"))
            val Created(publisher) = probe.expectMsgType[Created]

            connection ! Create(Props(new ChannelActor(setupSubscriber)), Some("subscriber"))
            val Created(subscriber) = probe.expectMsgType[Created]

            val msgs = (0L to 100)
            msgs.foreach(x =>
                publisher ! ChannelMessage(_.basicPublish(exchange, "", null, toBytes(x)), dropIfNoChannel = false))

            probe.expectMsgAllOf(msgs: _*)

            /*if (wordcount == 21) {
                println(Console.GREEN + Console.BOLD+"**SUCCESS**"+Console.RESET)
                bugDetected = false
            } else {
                println(Console.RED + Console.BOLD+"**FAILURE** word count is %d".format(wordcount) + Console.RESET)
                bugDetected = true
            }*/

        } catch {
            case e: AssertionError => {
                bugDetected = true
                println(Console.YELLOW + Console.BOLD+"**WARNING** %s".format(e.getMessage()) + Console.RESET)
            }

            case e: java.util.concurrent.TimeoutException => {
                bugDetected = true
                println(Console.YELLOW + Console.BOLD+"**WARNING** %s".format(e.getMessage()) + Console.RESET)
            }
        }
    }
}