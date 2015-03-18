package voters

import akka.actor.{ Actor, Props }

case object Vote

// Use bank.prop in the code or Voter()
// See http://doc.akka.io/docs/akka/snapshot/scala/actors.html#Recommended_Practices
object Voter {
    def props(): Props = Props(new Voter())
    def apply(): Props = Props(new Voter())
}

/**
 * Voter actor that sends elects himself in the ballot.
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
            println(Console.CYAN + Console.BOLD+"The voter %s was asked to vote".format(self.toString) + Console.RESET)
        }

        case msg => {
            throw new IllegalArgumentException("Unknown message "+msg)
        }
    }
}