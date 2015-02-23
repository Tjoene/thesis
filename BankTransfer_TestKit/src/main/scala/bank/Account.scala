package bank

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }

case object Balance
case class Transfer(dest: ActorRef, amount: Int)
case class Receive(amount: Int, from: String)

class Account(var holder: String, var amount: Int) extends Actor {
    
    def receive = {
        case Transfer(dest, amount) => { // Transfer an amount to the given destination
            println("%s: sends an amount of %d".format(holder, amount))
            
            if ((this.amount - amount) >= 0) {
                this.amount -= amount
                dest ! Receive(amount, this.holder)
            } else {
                throw new ArithmeticException("The bank doesn't give away money!");
            }
        }
        
        case Receive(amount, from) => { // Receive an incoming transfer
            println("%s: received an amount of %d from %s".format(holder, amount, from))
            this.amount += amount
        }
        
        case Balance => { // Give the current balance
            println("%s: someone requested the amount of this account, which is %d".format(holder, amount))
            sender ! this.amount
        }

        case _ => println("%s: 'FATAL ERROR'".format(holder))
    }
}