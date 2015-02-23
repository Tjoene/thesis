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

// class Bank1Spec extends FlatSpec with BeforeAndAfterEach {
//   implicit val timeout = Timeout(1500.millisecond)
//   var system: ActorSystem = _
//   var random = false

//   "The returned array" should "be sorted" in {
//     var initialSchedule = "./test-results/initial.txt"
//     run()
//     Scheduler.finish(initialSchedule)
//     var schedulesPath = "./test-results/generated-schedules/"
//     PRCriterion.generateSchedules("bank", Array(initialSchedule), schedulesPath)
//   }

//   "The returned array" should "be sorted with the first generated schedule" in {
//     Scheduler.setSchedule("./test-results/generated-schedules/bank-1-schedule.txt")
//     run()
//     Scheduler.finish("./test-results/execution/bank-1-schedule-execution.txt")
//   }
  
//   "The returned array" should "be sorted with a random scheduling" in {
//     random = true
//     run()
//     Scheduler.finish("./test-results/random-150/bank-random.txt")
//     random = false
//   }

//   override def afterEach() {
//     system.shutdown()
//     system.awaitTermination()
//     RandomScheduleHelper.reset()
//     Scheduler.reset()
//   }

//   def run() {
//     system = ActorSystem()
//     if (random) {
//       RandomScheduleHelper.setMaxDelay(150)
//       RandomScheduleHelper.setSystem(system)
//     }
    
//     var bankActor = system.actorOf(Props(new Bank(-1, system)))

//     bankActor ! Start // Start the simulation

//     //Thread.sleep(1000)

//     var result = Await.result(akka.bita.pattern.Patterns.ask(bankActor, Balance), timeout.duration)
//     assert(result.asInstanceOf[Int] == 500)
//   }
// }

// // Als je de assert aan laat, faalt de initial schedule, wordt deze nooit opgeslagen, en kunnen de andere testen
// // niet werken....
// // Ook is deze manier van werken niet wat wij willen!!!