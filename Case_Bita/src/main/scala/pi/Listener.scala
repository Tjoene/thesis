package pi

import akka.actor.{ Props, Actor }

// Use bank.prop in the code or Listener()
// See http://doc.akka.io/docs/akka/snapshot/scala/actors.html#Recommended_Practices
object Listener {
    def props(): Props = Props(new Listener())
    def apply(): Props = Props(new Listener())
}

class Listener extends Actor {
    def receive = {
        case PiApproximation(pi, duration) =>
            println("\n\tPi approximation: \t\t%s\n\tCalculation time: \t%s"
                .format(pi, duration))
            context.system.shutdown()
    }
}