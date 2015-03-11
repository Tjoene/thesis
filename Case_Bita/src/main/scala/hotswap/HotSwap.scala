package hotswap

/**
 * Source inspired from http://doc.akka.io/docs/akka/snapshot/scala/actors.html#become-unbecome
 */

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }

object HotSwap {
    def props(): Props = Props(new HotSwap())
    def apply(): Props = Props(new HotSwap())
}

case object Foo
case object Bar

class HotSwap extends Actor {
    import context._

    def angry: Receive = {
        case Bar => sender ! "I am already angry?"
        case Foo => become(happy)
    }

    def happy: Receive = {
        case Foo => sender ! "I am already happy :-)"
        case Bar => become(angry)
    }

    def receive = {
        case Foo => become(angry)
        case Bar => become(happy)
    }
}