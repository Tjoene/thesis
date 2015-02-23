// import akka.actor.ActorSystem
// import akka.actor.Props
// import akka.actor.Actor
// import akka.actor.ActorRef
// import akka.actor.actorRef2Scala
// import akka.actor.Props

// import akka.util.duration._

// import org.scalatest._

// import akka.bita.Scheduler
// import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
// import akka.bita.pattern.Patterns._
// import akka.dispatch.Await
// import akka.util.Timeout
// import akka.dispatch.DefaultPromise
// import akka.dispatch.Future
// import bita.util.FileHelper
// import bita.criteria._
// import bita.ScheduleOptimization._
// import bita.ScheduleOptimization
// import bita.util._
// import akka.bita.RandomScheduleHelper

// class BankSpec2() extends TestHelper with FlatSpec with BeforeAndAfterEach {
//     implicit val timeout = Timeout(1500.millisecond)
//     var random = false

//     val name = "bank"
//     var allTracesDir = "test-results/" + name + "/"

//     var generatedSchedulesNum = -1
//     //val criteria = Array[Criterion](PCRCriterion, PRCriterion )
//     val criteria = Array[Criterion](PRCriterion )
    
//     // Test the bank example with all criteria and optimizations.
//     "Different optimizations" should "be tested for bank" in {

//         var randomTraceDir = allTracesDir + "random/"
//         testRandom(name, randomTraceDir, 1)
//         var randomTrace = FileHelper.getFiles(randomTraceDir, (name => name.contains("-trace.txt")))

//         for (criterion <- criteria) {

//             for (opt <- criterion.optimizations) {

//                 var scheduleDir = allTracesDir + "schedule-%s-%s/".format(criterion.name, opt)
//                 FileHelper.emptyDir(scheduleDir)
//                 FileHelper.deleteFiles(scheduleDir, (name => name.contains("-schedule.txt") || name.contains("-schedule-trace.txt")))
//                 generateAndTestGeneratedSchedules(name, randomTrace, scheduleDir, criterion, opt)

//                 // Measure the coverage and output the results into resultFile.
//                 var resultFile = scheduleDir + "schedule%s-result.txt".format(opt)
//                 // Must consider the first random trace for measuring the coverage.
//                 FileHelper.copyFiles(randomTrace,scheduleDir)
//                 var traceFiles = FileHelper.getFiles(scheduleDir, (name => name.contains("-trace.txt")))
//                 generatedSchedulesNum = traceFiles.length
//                 criterion.measureCoverage(traceFiles, resultFile)
//             }
//         }
//     }

//     override def afterEach() {
//         system.shutdown()
//         system.awaitTermination()
//         RandomScheduleHelper.reset()
//         Scheduler.reset()
//     }

    // def run() {
    //     system = ActorSystem()
    //     if (random) {
    //         RandomScheduleHelper.setMaxDelay(150)
    //         RandomScheduleHelper.setSystem(system)
    //     }
        
    //     var bankActor = system.actorOf(Bank())

    //     bankActor ! Start // Start the simulation

    //     val future = ask(bankActor, RegisterSender)
    //     var result = Await.result(future, timeout.duration).asInstanceOf[Int]

    //     if(result == 500) {
    //         bugDetected = false
    //         println("**SUCCESS** Freddy has %d on his account".format(result))
    //     } else {
    //         bugDetected = true
    //         println("**FAILURE** Freddy has %d on his account".format(result))
    //     }
    // }
// }

// // Bij PMHRCriterion gaat ie een IO Exceptie werpen omdat ie de map niet zelf aanmaakt. Dummkopf!
// // Oplossen door beforeAll( Java.IO.newDir() )???