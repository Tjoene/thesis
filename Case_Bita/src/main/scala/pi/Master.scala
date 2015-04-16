package pi

import akka.actor.{ ActorRef, Props, Actor }
import akka.routing.RoundRobinRouter
import akka.util.duration._

// Use master.prop in the code or Master()
// See http://doc.akka.io/docs/akka/snapshot/scala/actors.html#Recommended_Practices
object Master {
    def props(nrOfWorkers: Int, nrOfElements: Int, nrOfMessages: Int, listener: ActorRef): Props =
        Props(new Master(nrOfWorkers, nrOfElements, nrOfMessages, listener))

    def apply(nrOfWorkers: Int, nrOfElements: Int, nrOfMessages: Int, listener: ActorRef): Props =
        Props(new Master(nrOfWorkers, nrOfElements, nrOfMessages, listener))
}

class Master(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int, listener: ActorRef)
        extends Actor {

    var pi: Double = _
    var nrOfResults: Int = _
    val start: Long = System.currentTimeMillis

    val workerRouter = context.actorOf(Worker().withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")

    def receive = {
        case Calculate =>
            for (i <- 0 until nrOfMessages) workerRouter ! Work(i * nrOfElements, nrOfElements)
        case Result(value) =>
            pi += value
            nrOfResults += 1
            if (nrOfResults == nrOfMessages) {
                //listener ! PiApproximation(pi, duration = (System.currentTimeMillis - start).millis)
                listener ! pi
                context.stop(self)
            }
    }
}
