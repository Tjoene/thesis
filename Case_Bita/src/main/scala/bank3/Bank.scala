package bank3

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.dispatch.Future
import akka.bita.pattern.Patterns.ask
import akka.util.duration._
import akka.util.Timeout
import akka.dispatch.Await

case object Start
case object RegisterSender
case object Finish

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
    var account1 : ActorRef = _
    var account2 : ActorRef = _
    var account3 : ActorRef = _
    var account4 : ActorRef = _
    implicit val timeout = Timeout(5000.millisecond)
    
    def receive = {
        case Start => {
            val testAmount = 5

            account1 = context.actorOf(Account("Freddy", testAmount, self, null)) // Create child actors that will host the accounts
            account4 = context.actorOf(Account("Charlie", 0, null, null))
            account3 = context.actorOf(Account("Stevie",  0, null, account4))
            account2 = context.actorOf(Account("Johnny",  0, self, account3))
         
            account1 ! Transfer(account2, testAmount)

            Thread.sleep(delay)

            account2 ! Withdraw(testAmount)
        }

        case Finish => {
            val future = ask(account4, Balance)
            val result = Await.result(future, timeout.duration).asInstanceOf[Int]

            println(Console.YELLOW + Console.BOLD + "BANK:   registered an amount of %d".format(result)+ Console.RESET)

            if(dest != null){
                dest ! result
            } else {
                self ! Finish
            }
        } 
        
        // This will register the test as the destination where we need to send 
        // the balance to when we receive the finish signal
        case RegisterSender => { 
            dest = sender
        }

        case _ => println(Console.YELLOW + Console.BOLD + "BANK: 'FATAL ERROR'"+ Console.RESET)
    }
}