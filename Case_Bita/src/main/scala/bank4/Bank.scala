package bank4

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.dispatch.Future
import akka.bita.pattern.Patterns.ask
import akka.util.duration._
import akka.util.Timeout
import akka.dispatch.Await
import akka.testkit.CallingThreadDispatcher

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
            println(Console.GREEN + Console.BOLD + "BANK:   We have started"+ Console.RESET)
            val testAmount = 5

            account1 = context.actorOf(Account("Freddy", testAmount, self, null).withDispatcher(CallingThreadDispatcher.Id), "Account_Freddy") // Create child actors that will host the accounts
            account4 = context.actorOf(Account("Charlie", 0, null, null).withDispatcher(CallingThreadDispatcher.Id), "Account_Charlie")
            account3 = context.actorOf(Account("Stevie",  0, null, account4).withDispatcher(CallingThreadDispatcher.Id), "Account_Stevie")
            account2 = context.actorOf(Account("Johnny",  0, self, account3).withDispatcher(CallingThreadDispatcher.Id), "Account_Johnny")
         
            account1 ! Transfer(account2, testAmount)

            Thread.sleep(delay)

            account2 ! Withdraw(testAmount)
        }

        case Finish => {
            val future = ask(account4, Balance)
            val result = Await.result(future, timeout.duration).asInstanceOf[Int]

            println(Console.GREEN + Console.BOLD + "BANK:   registered an amount of %d".format(result)+ Console.RESET)
            
            dest ! result
            /*if(dest != null){
                dest ! result
            } else {
                self ! Finish
            }*/
        } 
        
        // This will register the test as the destination where we need to send 
        // the balance to when we receive the finish signal
        case RegisterSender => { 
            println(Console.GREEN + Console.BOLD + "BANK:   the testcase has been registered"+ Console.RESET)
            dest = sender
        }

        case _ => println(Console.RED + Console.BOLD + "BANK: 'FATAL ERROR'"+ Console.RESET)
    }
}