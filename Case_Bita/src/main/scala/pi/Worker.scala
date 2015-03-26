package pi

import akka.actor.{ Actor, Props }

// Use bank.prop in the code or Bank()
// See http://doc.akka.io/docs/akka/snapshot/scala/actors.html#Recommended_Practices
object Worker {
    def props(): Props = Props(new Worker())
    def apply(): Props = Props(new Worker())
}

class Worker extends Actor {

    def calculatePiFor(start: Int, nrOfElements: Int): Double = {
        var acc = 0.0
        for (i <- start until (start + nrOfElements)) {
            acc += 4.0 * (1 - (i % 2) * 2) / (2 * i + 1)
        }
        acc
    }

    def receive = {
        case Work(start, nrOfElements) => {
            sender ! Result(calculatePiFor(start, nrOfElements)) // perform the work
        }
    }

}
