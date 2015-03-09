package bank2

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.dispatch.Future

case object Start
case class Finish(amount: Int)

// Use bank.prop in the code or Bank() or Bank(-1)
// See http://doc.akka.io/docs/akka/snapshot/scala/actors.html#Recommended_Practices
object Bank {
    def props(): Props = Props(new Bank(0, null))
    def apply(): Props = Props(new Bank(0, null))

    def props(delay: Int): Props = Props(new Bank(delay, null))
    def apply(delay: Int): Props = Props(new Bank(delay, null))

    def props(delay: Int, dest: ActorRef): Props = Props(new Bank(delay, dest))
    def apply(delay: Int, dest: ActorRef): Props = Props(new Bank(delay, dest))
}

class Bank(val delay: Int, val dest: ActorRef) extends Actor {

    def receive = {
        case Start => {
            val testAmount = 5

            // Create child actors that will host the accounts
            val account3 = context.actorOf(Account("Stevie", 0, null, null), "Account_Stevie")
            val account2 = context.actorOf(Account("Johnny", 0, self, account3), "Account_Johnny")
            val account1 = context.actorOf(Account("Freddy", testAmount, null, null), "Account_Freddy") 

            account1 ! Transfer(account2, testAmount)

            Thread.sleep(delay)

            account2 ! Withdraw(testAmount)
        }

        case Finish(amount) => {
            println(Console.YELLOW + Console.BOLD+"BANK:   registered an amount of %d".format(amount) + Console.RESET)
            dest ! amount
        }

        case _ => println(Console.YELLOW + Console.BOLD+"BANK: 'FATAL ERROR'"+Console.RESET)
    }
}