package bank2

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.bita.pattern.Patterns.ask
import akka.util.duration._
import akka.util.Timeout
import akka.dispatch.Await

case object Balance
case object Continue
case class Transfer(dest: ActorRef, amount: Int)
case class Withdraw(amount: Int)
case class Deposit(amount: Int, from: String)

object Account {
    def props(): Props = Props(new Account("Dummy", 0, null, null))
    def apply(): Props = Props(new Account("Dummy", 0, null, null))

    def props(holder: String, amount: Int, bank: ActorRef, next: ActorRef): Props = Props(new Account(holder, amount, bank, next))  
    def apply(holder: String, amount: Int, bank: ActorRef, next: ActorRef): Props = Props(new Account(holder, amount, bank, next))
}

class Account(var holder: String, var amount: Int, var bank: ActorRef, var next: ActorRef) extends Actor {
    implicit val timeout = Timeout(5000.millisecond)
    
    def receive = {
        case Transfer(dest, amount) => { // Transfer an amount to the given destination
            println(Console.YELLOW + Console.BOLD + "%s: sends an amount of %d".format(holder, amount) + Console.RESET)

            this.amount -= amount
            dest ! Deposit(amount, this.holder)
        }
        
        case Withdraw(amount) => {
            println(Console.YELLOW + Console.BOLD + "%s: withdrawing an amount of %d".format(holder, amount) + Console.RESET)
            this.amount -= amount
        }

        case Deposit(amount, from) => { // Deposit an incoming transfer
            println(Console.YELLOW + Console.BOLD + "%s: received an amount of %d from %s".format(holder, amount, from) + Console.RESET)
            this.amount += amount

            if (next != null) { // only send the message if we have a target account to send our money to.
                self ! Continue 
            }
        }

        case Continue => {
            if(this.amount > 0){
                println(Console.YELLOW + Console.BOLD + "*CONTINUE* %s: amount that remains is %d".format(holder, amount) + Console.RESET)
                next ! Deposit(1, this.holder)
                this.amount -= 1;

                self ! Continue
            } else {
                println(Console.YELLOW + Console.BOLD + "*CONTINUE* %s: we are finished here, notifing the bank".format(holder) + Console.RESET)

                val future = ask(next, Balance)
                val result = Await.result(future, timeout.duration).asInstanceOf[Int]
                bank ! Finish(result)
            }
        }
        
        case Balance => { // Give the current balance
            println(Console.YELLOW + Console.BOLD + "%s: someone requested the amount of this account, which is %d".format(holder, amount) + Console.RESET)
            sender ! this.amount
        }

        case _ => println(Console.YELLOW + Console.BOLD + "%s: 'FATAL ERROR'".format(holder) + Console.RESET)
    }
}