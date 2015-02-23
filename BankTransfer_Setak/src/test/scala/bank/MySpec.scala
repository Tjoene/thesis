package bank

import akka.actor.Actor
import akka.actor.ActorRef
import org.junit.Test
import org.junit.Before
import org.junit.After
import akka.setak.core.TestMessageEnvelop
import akka.setak.core.TestMessageEnvelopSequence._
import akka.setak.core.TestActorRef
import akka.setak.SetakJUnit
import akka.setak.SetakFlatSpec
import akka.setak.Commons._

class MySpec extends SetakJUnit {

    var account1: ActorRef = _
    var account2: ActorRef = _

    @Before
    def setUp() {
        // Freddy creates an account and puts 500 dollar on it.
        account1 = actorOf(new Account("Freddy", 500)).start
        
        // Johnny creates one to, but can't affort to put any money on it.
        account2 = actorOf(new Account("Johnny", 0)).start
    }

    private def process() {
        account1 ! Transfer(account2, 500) // Freddy is generous and gives all his money to Johnny.        
        account1 ! Receive(50, "The Bank") // The bank rewards Freddy for his transaction.

        val result = (account1 ? Balance).mapTo[Int].get // Freddy wants to view his saldo on his account.
        assert(result == 50, "The returned value is not 50, but %d".format(result))
    }

    @Test
    def correctBalance1() {
        val transfer1 = testMessageEnvelop(anyActorRef, account1, Transfer(account2, 500))
        val receive1  = testMessageEnvelop(anyActorRef, account1, Receive(50, "The Bank"))
        val balance1  = testMessageEnvelop(anyActorRef, account1, Balance)

        setSchedule(transfer1 -> receive1 -> balance1)
        process()
    }

    @Test
    def correctBalance2() {
        val transfer1 = testMessageEnvelop(anyActorRef, account1, Transfer(account2, 500))
        val receive1  = testMessageEnvelop(anyActorRef, account1, Receive(50, "The Bank"))
        val balance1  = testMessageEnvelop(anyActorRef, account1, Balance)

        setSchedule(receive1 -> transfer1 -> balance1)
        process()
    }

    @Test
    def wrongBalance1() {
        val transfer1 = testMessageEnvelop(anyActorRef, account1, Transfer(account2, 500))
        val receive1  = testMessageEnvelop(anyActorRef, account1, Receive(50, "The Bank"))
        val balance1  = testMessageEnvelop(anyActorRef, account1, Balance)

        setSchedule(balance1 -> transfer1 -> receive1)
        process()
    }

    @Test
    def wrongBalance2() {
        val transfer1 = testMessageEnvelop(anyActorRef, account1, Transfer(account2, 500))
        val receive1  = testMessageEnvelop(anyActorRef, account1, Receive(50, "The Bank"))
        val balance1  = testMessageEnvelop(anyActorRef, account1, Balance)

        setSchedule(balance1 -> receive1 -> transfer1)
        process()
    }
}