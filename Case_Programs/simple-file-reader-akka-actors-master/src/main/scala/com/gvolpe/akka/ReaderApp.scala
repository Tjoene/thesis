package com.gvolpe.akka

import actors._
import akka.actor.{ ActorSystem, Props }
import akka.dispatch.ExecutionContexts._
import akka.pattern.ask
import akka.util.Timeout
import akka.util._
import akka.util.duration._

object ReaderApp extends App {

  implicit val timeout = Timeout(25 seconds)

  val system = ActorSystem("FileReaderSystem")
  val actor = system.actorOf(WordCounterActor.props("files/rockbands.txt"))

  val futureCount = actor ? StartProcessFileMsg
//  actor ? StartProcessFileMsg // Will print that Process is already running!
//	actor ? StartProcessFileMsg // Will print that Process is already running!

  futureCount map { result =>
    println("Total words in file: " + result)
    system.shutdown
  }

}