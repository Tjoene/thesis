package bank

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.dispatch.Future

case object Start
case object RegisterSender

// Use bank.prop in the code or Bank() or Bank(-1)
// See http://doc.akka.io/docs/akka/snapshot/scala/actors.html#Recommended_Practices
object Bank {
    def props(): Props = Props(new Bank())
    def apply(): Props = Props(new Bank())
}

class Bank() extends Actor {
    import context.dispatcher

    var dest : ActorRef = _

    def receive = {
        case Start => {
            var account1 = context.actorOf(Account("Freddy", 500), "Account_Freddy") // Create child actors that will host the accounts
            var account2 = context.actorOf(Account("Johnny", 0), "Account_Johnny")
         
            account1 ! Transfer(account2, 500) // Freddy makes a transaction to Johnny for an amount of 500.

            // The transfer should lead to an implicit receive:
            //account2! Receive(50, "Freddy")

            account2 ! Balance // Return the amount of account Johnny, should be 500
        }

        case amount:Int => {
            println(Console.YELLOW + Console.BOLD + "BANK:   registered an amount of %d".format(amount) + Console.RESET)
            dest ! amount
        }
        
        case RegisterSender => { // Give the current balance
            dest = sender
        }

        case _ => println(Console.YELLOW + Console.BOLD + "BANK: 'FATAL ERROR'" + Console.RESET)
    }
}