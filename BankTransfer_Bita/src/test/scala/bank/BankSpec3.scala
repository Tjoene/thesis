// // GOOD AND WORKING TEST CASE

// package bank

// import scala.collection.mutable.{ HashMap, ArrayBuffer, Queue }
// import akka.bita.Scheduler
// import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
// import akka.bita.pattern.Patterns._
// import akka.dispatch.Await
// import akka.util.duration._
// import akka.util.Timeout
// import akka.dispatch.DefaultPromise
// import akka.dispatch.{ Promise, Future }
// import bita.{ ScheduleEnvelope, LogicalMessage, EventID }
// import bita.util.FileHelper
// import bita.criteria._
// import bita.ScheduleOptimization._
// import bita.ScheduleOptimization
// import bita.util.TestHelper
// import org.scalatest._
// import akka.bita.RandomScheduleHelper
// import akka.bita.pattern.Patterns.ask
// import com.typesafe.config.ConfigFactory

// class Bank3Spec extends TestHelper with FunSpec{

//     val name = "bank"

//     // feel free to change these parameters to test the bank with various configurations.
//     implicit val timeout = Timeout(5000.millisecond)

//     // test with PR criterion
//     val criterion = PRCriterion

//     var allTracesDir = "test-results/%s/".format(name)
//     var randomTracesDir = allTracesDir + "random/"
//     var randomTracesTestDir = allTracesDir + "random-test/"

//     var generatedSchedulesNum = -1

//     describe("Bank Test") {

//         // it(" should test randomly within a timeout", Tag("random-timeout")) {
//         //     testRandomByTime(name, randomTracesTestDir, 10) // 10 sec timeout
//         // }

//         // Generates a random trace which will be used for schedule generation.
//         it(" should generate a random trace", Tag("random")) {
//             FileHelper.emptyDir(randomTracesDir)
//             var traceFiles = FileHelper.getFiles(randomTracesDir, (name => name.contains("-trace.txt")))
//             var traceIndex = traceFiles.length + 1
//             var newTraceName = name + "-random%s-trace.txt".format(traceIndex)
//             testRandom(name, randomTracesDir, 1)
//         }

//         // it(" should generate schedules ", Tag("generate")) {
//         //     var randomTrace = FileHelper.getFiles(randomTracesDir, (name => name.contains("-trace.txt")))
//         //     for (opt <- criterion.optimizations.-(NONE)) {
//         //         var scheduleDir = allTracesDir + "%s-%s/schedules/".format(criterion.name, opt)
//         //         FileHelper.emptyDir(scheduleDir)
//         //         generateSchedules(name, randomTrace, scheduleDir, criterion, opt, -1)
//         //     }
//         // }

//         // it(" should test the generated schedules ", Tag("test")) {
//         //     for (opt <- criterion.optimizations.-(NONE)) {
//         //         var scheduleDir = allTracesDir + "%s-%s/schedules/".format(criterion.name, opt)

//         //         var traceFiles = FileHelper.getFiles(scheduleDir, (name => name.contains("-trace.txt")))
//         //         var scheduleIndex = traceFiles.length + 1
//         //         var newScheduleFileName = name + "-%s-schedule.txt".format(scheduleIndex)
//         //         testGeneratedSchedules(scheduleDir)
//         //     }
//         // }

//         it(" should generate and test schedules ", Tag("generate-test")) {
//             var randomTrace = FileHelper.getFiles(randomTracesDir, (name => name.contains("-trace.txt")))
//             for (opt <- criterion.optimizations.-(NONE)) {
//                 var scheduleDir = allTracesDir + "%s-%s/".format(criterion.name, opt)
                
//                 FileHelper.emptyDir(scheduleDir)
//                 generateAndTestGeneratedSchedules(name, randomTrace, scheduleDir, criterion, opt, -1)
//             }
//         }

//         // it(" should measure the coverage of testing with schedules ", Tag("coverage")) {
//         //     // The number of traces after which the coverage should be measured.
//         //     var interval = 5
//         //     for (opt <- criterion.optimizations.-(NONE)) {
//         //         var scheduleDir = allTracesDir + "%s-%s/".format(criterion.name, opt)
//         //         var randomTraces = FileHelper.getFiles(randomTracesDir, (name => name.contains("-trace.txt")))
//         //         FileHelper.copyFiles(randomTraces, scheduleDir)

//         //         var resultFile = scheduleDir + "%s-%s-result.txt".format(criterion.name, opt)
//         //         var traceFiles = FileHelper.getFiles(scheduleDir, (name => name.contains("-trace.txt")))
//         //         traceFiles = FileHelper.sortTracesByName(traceFiles, "-%s-")
//         //         criterion.measureCoverage(traceFiles, resultFile, interval)
//         //     }
//         // }
//     }

//     def run {
//         system = ActorSystem()
//         RandomScheduleHelper.setSystem(system)

//         var bankActor = system.actorOf(Bank())

//         bankActor ! Start // Start the simulation

//         val future = ask(bankActor, RegisterSender)
//         var result = Await.result(future, timeout.duration).asInstanceOf[Int]

//         if(result == 500) {
//             bugDetected = false
//             println("**SUCCESS** Freddy has %d on his account".format(result))
//         } else {
//             bugDetected = true
//             println("**FAILURE** Freddy has %d on his account".format(result))
//         }
//     }
// }
