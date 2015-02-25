package bank

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.dispatch.Future
import akka.pattern.pipe

case object Start

// Use bank.prop in the code or Bank() or Bank(-1)
// See http://doc.akka.io/docs/akka/snapshot/scala/actors.html#Recommended_Practices
object Bank {
    def props(): Props = Props(new Bank(-1))
    def apply(): Props = Props(new Bank(-1))

    def props(amount: Int): Props = Props(new Bank(amount))
    def apply(amount: Int): Props = Props(new Bank(amount))
}

class Bank(var amount: Int) extends Actor {
    import context.dispatcher

    def receive = {
        case Start => {
            var account1 = context.actorOf(Account("Freddy", 500))
            var account2 = context.actorOf(Account("Johnny", 0))

            account1 ! Transfer(account2, 500) // Freddy makes a transaction to Johnny for an amount of 500.

            // The transfer should lead to an implicit receive:
            //account2! Receive(50, "Freddy")

            Thread.sleep(50);

            account2 ! Balance // Return the amount of account Johnny, should be 500
        }

        case x:Int => {
            println(Console.YELLOW + Console.BOLD + "BANK:   registered an amount of %d".format(x) + Console.RESET)
            this.amount = x
        } 
        
        case Balance => { // Give the current balance
            Future {
                while (this.amount == -1) {} // wait until we have a valid amount
                this.amount
            } pipeTo sender
        }

        case _ => println(Console.YELLOW + Console.BOLD + "BANK: 'FATAL ERROR'" + Console.RESET)
    }

    def setAmount(amount: Int) = {
        this.amount = amount
    }

    def getAmount() = {
        this.amount
    }
}