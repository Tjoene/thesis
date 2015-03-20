package pi

import akka.actor.ActorSystem

object Pi extends App {
    calculate(nrOfWorkers = 4, nrOfElements = 10000, nrOfMessages = 10000)

    def calculate(nrOfWorkers: Int, nrOfElements: Int, nrOfMessages: Int): Unit = {
        val system = ActorSystem("PiSystem")
        val listener = system.actorOf(Listener.props, "listener")
        val master = system.actorOf(Master.props(nrOfWorkers, nrOfElements, nrOfMessages, listener), "master")

        master ! Calculate
    }
}