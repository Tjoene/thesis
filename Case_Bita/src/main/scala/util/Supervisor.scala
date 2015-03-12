package util

import akka.actor.{ Actor, Props, PoisonPill }

/**
 * Factory object for the Supervisor Actor
 * Call props with the constructor args
 */
object Supervisor {
    def props(): Props = Props(new Supervisor())
    def apply(): Props = Props(new Supervisor())
}

/**
 * Supervisor actor that will house all the actors as childeren
 * during testing
 */
class Supervisor() extends Actor {

    import context.dispatcher

    def receive = {
        case CreateClass(actorClass, name) => {
            val tmp = context.actorOf(Props(actorClass), name)
            sender ! tmp
        }

        case CreateProp(actorProp, name) => {
            val tmp = context.actorOf(actorProp, name)
            sender ! tmp
        }

        case Stop => {
            context.children foreach { child =>
                child ! PoisonPill
                context.unwatch(child)
                context.stop(child)
            }
        }

        case msg => {
            throw new IllegalArgumentException("Unknown message "+msg)
        }
    }
}

case class CreateClass(actorClass: Class[_ <: Actor], name: String)
case class CreateProp(actorProp: Props, name: String)

case object Stop