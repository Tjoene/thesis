package voters

import akka.actor.{ Actor, ActorRef, Props }

// Use bank.prop in the code or Ballot()
// See http://doc.akka.io/docs/akka/snapshot/scala/actors.html#Recommended_Practices
object Ballot {
    def props(): Props = Props(new Ballot())
    def apply(): Props = Props(new Ballot())
}

/**
 * Ballot actor, this will hold the election and give the winner.
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