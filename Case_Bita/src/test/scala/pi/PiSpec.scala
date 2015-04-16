// // CONTAINS A BUG WHEN DURING SHUTTING ACTOR SYSTEM DOWN.

// package pi

// import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
// import akka.bita.RandomScheduleHelper
// import akka.bita.pattern.Patterns._
// import akka.util.duration._
// import org.scalatest._
// import akka.testkit.TestProbe

// import util.BitaTests

// class PiSpec extends BitaTests {

//     // The name of this test battery
//     override def name = "pi"

//     override def expectFailures = false

//     // This will hold the actor/testcase/application under test
//     def run {
//         system = ActorSystem("ActorSystem")
//         if (random) {
//             RandomScheduleHelper.setMaxDelay(250) // Increase the delay between messages to 250 ms
//             RandomScheduleHelper.setSystem(system)
//         }

//         try {
//             val probe = new TestProbe(system) // Use a testprobe to represent the tests.

//             val listener = system.actorOf(Listener.props, "listener")
//             val master = system.actorOf(Master(4, 100, 100, probe.ref), "master")

//             probe.send(master, Calculate)

//             val result = probe.expectMsgType[Double](timeout.duration)
//             if (result == 3.1415926535897932384626433832) {
//                 println(Console.GREEN + Console.BOLD+"**SUCCESS**"+Console.RESET)
//                 bugDetected = false
//             } else {
//                 println(Console.RED + Console.BOLD+"**FAILURE**"+Console.RESET)
//                 bugDetected = true
//             }

//             system.shutdown()
//         } catch {
//             case e: AssertionError => {
//                 bugDetected = true
//                 println(Console.YELLOW + Console.BOLD+"**WARNING** %s".format(e.getMessage()) + Console.RESET)
//             }

//             case e: java.util.concurrent.TimeoutException => {
//                 bugDetected = true
//                 println(Console.YELLOW + Console.BOLD+"**WARNING** %s".format(e.getMessage()) + Console.RESET)
//             }
//         }
//     }
// }