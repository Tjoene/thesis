package akka.bita
package pattern

/**
 * @author Samira Tasharofi (tasharo1@illinois.edu)
 */

import akka.actor.ActorRef
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import akka.util.Timeout
import akka.dispatch.Envelope
import akka.actor.ActorSystem
import akka.actor.InternalActorRef
import scala.concurrent.Promise
import akka.pattern.AskTimeoutException
import akka.pattern.PromiseActorRef
import Scheduler._
import bita.util._

object Patterns {

    /**
     * The calls for <code>ask</code> or <code>?</code> to the Akka library should be replaced
     * with a call to this method.
     * The purpose is to record some information which will be used when computing happens-before
     * relation and enforcing a schedule.
     */
    def ask(actorRef: ActorRef, message: Any)(implicit sender: ActorRef = null, timeout: Timeout): Future[Any] = {
        actorRef match {
            case ref: InternalActorRef if ref.isTerminated =>
                actorRef.tell(message, null)
                val p = Promise[Any]
                (p failure (new AskTimeoutException("sending to terminated ref breaks promises"))).future //(ref.provider.dispatcher)
            case ref: InternalActorRef =>
                val provider = ref.provider
                if (timeout.duration.length <= 0) {
                    actorRef.tell(message, null)
                    val p = Promise[Any]
                    (p failure (new AskTimeoutException("not asking with negative timeout"))).future //(provider.dispatcher)
                } else {
                    val a = PromiseActorRef(provider, timeout, actorRef.toString())
                    if (sender == null) {
                        Scheduler.addPromiseToParentMap(a, ActorPathHelper.DeadLetterActorPath)
                    } else {
                        Scheduler.addPromiseToParentMap(a, sender.path.toString())
                    }
                    if (!actorRef.isTerminated)
                        actorRef.tell(message, a)
                    var res = a.result
                    return res.future
                }
            case _ => throw new IllegalArgumentException("incompatible ActorRef "+actorRef)
        }
    }
}
