package voters

import akka.actor._
import akka.event._
import akka.dispatch._

/**
 * Sink Actor messaging test class to count received messages.
 *
 */
class Ballot() extends Actor {
    var won: ActorRef = _
    var voters: List[ActorRef] = _

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
        case Start(voters) => {
            this.voters = voters
            voters.foreach(_ ! Vote)
        }

        case elected: ActorRef => {
            won = sender
        }

        case Result => {
            println(Console.CYAN + Console.BOLD+"**SUCCESS** The ballot was asked what the result was"+Console.RESET)

            if (voters.contains(won)) {
                sender ! won
            } else {
                throw new TimingException("Too early!")
            }
        }

        case msg => {
            throw new IllegalArgumentException("Unknown message "+msg)
        }
    }
}

case class Start(voters: List[ActorRef])
case object Result

case class TimingException(msg: String) extends Exception