package pi

import akka.actor.ActorSystem

object Pi extends App {
    calculate(nrOfWorkers = 4, nrOfElements = 10000, nrOfMessages = 10000)

    def calculate(nrOfWorkers: Int, nrOfElements: Int, nrOfMessages: Int) {
        // Create an Akka system
        val system = ActorSystem("PiSystem")

        // create the result listener, which will print the result and shutdown the system
        val listener = system.actorOf(Listener(), name = "listener")

        // create the master
        val master = system.actorOf(Master(nrOfWorkers, nrOfMessages, nrOfElements, listener), name = "master")

        // start the calculation
        master ! Calculate

    }
}