package bank3

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }

case object Balance
case class Transfer(dest: ActorRef, amount: Int)
case class Receive(amount: Int, from: String)

object Account {
    def props(): Props = Props(new Account("Dummy", 0))
    def apply(): Props = Props(new Account("Dummy", 0))

    def props(holder: String, amount: Int): Props = Props(new Account(holder, amount))  
    def apply(holder: String, amount: Int): Props = Props(new Account(holder, amount))
}

class Account(var holder: String, var amount: Int) extends Actor {
    
    def receive = {
        case Transfer(dest, amount) => { // Transfer an amount to the given destination
            println(Console.YELLOW + Console.BOLD + "%s: sends an amount of %d".format(holder, amount) + Console.RESET)
            
            if ((this.amount - amount) >= 0) {
                this.amount -= amount
                dest ! Receive(amount, this.holder)
            } else {
                throw new ArithmeticException("The bank doesn't give away money!");
            }
        }
        
        case Receive(amount, from) => { // Receive an incoming transfer
            println(Console.YELLOW + Console.BOLD + "%s: received an amount of %d from %s".format(holder, amount, from) + Console.RESET)
            this.amount += amount
        }
        
        case Balance => { // Give the current balance
            println(Console.YELLOW + Console.BOLD + "%s: someone requested the amount of this account, which is %d".format(holder, amount) + Console.RESET)
            sender ! this.amount
        }

        case _ => println(Console.YELLOW + Console.BOLD + "%s: 'FATAL ERROR'".format(holder) + Console.RESET)
    }
}