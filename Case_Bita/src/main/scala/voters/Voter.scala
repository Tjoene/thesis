package voters

import akka.actor.Actor
import akka.actor.ActorRef

import akka.actor.ActorSystem

/**
 * Sink Actor messaging test class to count received messages.
 *
 */
class Voter() extends Actor {

    /**
     * Waits for incoming messages
     *
     * @message Vote replies with its own actorRef (as casted vote)
     *
     * @message msg
     * 			default message, throws an IllegalArgumentException
     *
     */

    def receive = {
        case Vote => {
            sender ! self
            println(Console.YELLOW + Console.BOLD+"**SUCCESS** The voter %s was asked to vote".format(self.toString) + Console.RESET)
        }

        case msg => {
            throw new IllegalArgumentException("Unknown message "+msg)
        }
    }
}

case object Vote

