package bank3

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.dispatch.Future
import akka.bita.pattern.Patterns.ask
import akka.util.duration._
import akka.util.Timeout
import akka.dispatch.Await

case object Start
case object Finish

// Use bank.prop in the code or Bank() or Bank(-1)
// See http://doc.akka.io/docs/akka/snapshot/scala/actors.html#Recommended_Practices
object Bank {
    def props(): Props = Props(new Bank(0, null))
    def apply(): Props = Props(new Bank(0, null))

    def props(delay: Int): Props = Props(new Bank(delay, null))
    def apply(delay: Int): Props = Props(new Bank(delay, null))

    def props(delay: Int, test: ActorRef): Props = Props(new Bank(delay, test))
    def apply(delay: Int, test: ActorRef): Props = Props(new Bank(delay, test))
}

class Bank(val delay: Int, val test: ActorRef) extends Actor {
    var lastAccount: ActorRef = _
    implicit val timeout = Timeout(5000.millisecond)

    def receive = {
        case Start => {
            val testAmount = 5

            lastAccount = context.actorOf(Account("Charlie", 0, null, null), "Account_Charlie")
            val account3 = context.actorOf(Account("Stevie", 0, null, lastAccount), "Account_Stevie")
            val account2 = context.actorOf(Account("Johnny", 0, self, account3), "Account_Johnny")
            val account1 = context.actorOf(Account("Freddy", testAmount, self, null), "Account_Freddy") // Create child actors that will host the accounts

            account1 ! Transfer(account2, testAmount)

            Thread.sleep(delay)

            account2 ! Withdraw(testAmount)
        }

        case Finish => {
            val future = ask(lastAccount, Balance)
            val result = Await.result(future, timeout.duration).asInstanceOf[Int]

            println(Console.YELLOW + Console.BOLD+"BANK:   registered an amount of %d".format(result) + Console.RESET)

            test ! result
        }

        case _ => println(Console.YELLOW + Console.BOLD+"BANK: 'FATAL ERROR'"+Console.RESET)
    }
}