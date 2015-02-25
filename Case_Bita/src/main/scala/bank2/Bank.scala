package bank2

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.dispatch.Future

case object Start
case object RegisterSender
case class Finish(amount: Int)

// Use bank.prop in the code or Bank() or Bank(-1)
// See http://doc.akka.io/docs/akka/snapshot/scala/actors.html#Recommended_Practices
object Bank {
    def props(): Props = Props(new Bank(0))
    def apply(): Props = Props(new Bank(0))

    def props(delay: Int): Props = Props(new Bank(delay))
    def apply(delay: Int): Props = Props(new Bank(delay))
}

class Bank(val delay: Int) extends Actor {
    var dest : ActorRef = _

    def receive = {
        case Start => {
            val testAmount = 5

            val account1 = context.actorOf(Account("Freddy", testAmount, self, null)) // Create child actors that will host the accounts
            val account3 = context.actorOf(Account("Stevie", 0, self, null))
            val account2 = context.actorOf(Account("Johnny", 0, self, account3))
         
            account1 ! Transfer(account2, testAmount)

            Thread.sleep(delay)

            account2 ! Withdraw(testAmount)
        }

        case Finish(amount) => {
            println(Console.YELLOW + Console.BOLD + "BANK:   registered an amount of %d".format(amount)+ Console.RESET)
            dest ! amount
        } 
        
        // This will register the test as the destination where we need to send 
        // the balance to when we receive the finish signal
        case RegisterSender => { 
            dest = sender
        }

        case _ => println(Console.YELLOW + Console.BOLD + "BANK: 'FATAL ERROR'"+ Console.RESET)
    }
}