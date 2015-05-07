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
    def props(): Props = Props(new Bank(0, 0))
    def apply(): Props = Props(new Bank(0, 0))

    def props(delay: Int): Props = Props(new Bank(delay, 0))
    def apply(delay: Int): Props = Props(new Bank(delay, 0))

    def props(delay: Int, extraAccounts: Int): Props = Props(new Bank(delay, extraAccounts))
    def apply(delay: Int, extraAccounts: Int): Props = Props(new Bank(delay, extraAccounts))
}

class Bank(val delay: Int, val extraAccounts: Int) extends Actor {
    var lastAccount: ActorRef = _
    implicit val timeout = Timeout(5000.millisecond)
    var dest: ActorRef = _

    def receive = {
        case Start => {
            dest = sender // register the test as destination

            val testAmount = 5

            // Create child actors that will host the accounts
            lastAccount = context.actorOf(Account("Charlie", 0, null, null), "Account_Charlie")

            // Dynamicly create extra actors
            var accounts: List[ActorRef] = List(lastAccount);
            for (i <- 0 to extraAccounts) {
                accounts = accounts ::: List(context.actorOf(Account("Account%d".format(i), 0, null, accounts.last), "Account_%d".format(i)))
            }

            val account3 = context.actorOf(Account("Stevie", 0, null, accounts.last), "Account_Stevie")
            val account2 = context.actorOf(Account("Johnny", 0, self, account3), "Account_Johnny")
            val account1 = context.actorOf(Account("Freddy", testAmount, null, null), "Account_Freddy")

            account1 ! Transfer(account2, testAmount)

            Thread.sleep(delay)

            account2 ! Withdraw(testAmount)
        }

        case Finish => {
            val future = ask(lastAccount, Balance)
            val result = Await.result(future, timeout.duration).asInstanceOf[Int]

            println(Console.YELLOW + Console.BOLD+"BANK:   registered an amount of %d".format(result) + Console.RESET)

            dest ! result // send the result to the test
        }

        case _ => println(Console.YELLOW + Console.BOLD+"BANK: 'FATAL ERROR'"+Console.RESET)
    }
}
