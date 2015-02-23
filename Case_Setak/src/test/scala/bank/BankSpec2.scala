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

class BankSpec2 extends SetakJUnit {

    akka.setak.TestConfig.maxTryForStability = 10
    akka.setak.TestConfig.sleepInterval = 10
    akka.setak.TestConfig.timeOutForMessages = 500

    var account1: ActorRef = _
    var account2: ActorRef = _

    @Before
    def setUp() {
        // Freddy creates an account and puts 500 dollar on it.
        account1 = actorOf(new Account("Freddy", 500)).start
        
        // Johnny creates one to, but can't affort to put any money on it.
        account2 = actorOf(new Account("Johnny", 0)).start
    }

    private def process(expected: Int) {
        account1 ! Transfer(account2, 500) // Freddy is generous and gives all his money to Johnny.        

        val result = (account2 ? Balance).mapTo[Int].get // Freddy wants to view his saldo on his account.
        assert(result == expected, "Expected %d, but got %d".format(expected, result))
    }

    @Test
    def correctBalance1() {
        val transfer1 = testMessageEnvelop(anyActorRef, account1, Transfer(account2, 500))
        val receive1  = testMessageEnvelop(account1,    account2, Receive(500, "Freddy"))
        val balance1  = testMessageEnvelop(anyActorRef, account1, Balance)
        val balance2  = testMessageEnvelop(anyActorRef, account2, Balance)

        setSchedule(transfer1 -> receive1, receive1 -> balance2, balance1 -> balance2)
        process(500)
    }

    @Test
    def wrongBalance1() {
        val transfer1 = testMessageEnvelop(anyActorRef, account1, Transfer(account2, 500))
        val receive1  = testMessageEnvelop(account1,    account2, Receive(500, "Freddy"))
        val balance1  = testMessageEnvelop(anyActorRef, account1, Balance)
        val balance2  = testMessageEnvelop(anyActorRef, account2, Balance)

        setSchedule(transfer1 -> receive1, balance2 -> receive1)
        process(0)
    }
}