package chehao.remote

import com.typesafe.config.ConfigFactory

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala

class RemoteActor extends Actor {
  override def receive: Receive = {
    case msg: String => {
      println("remote received " + msg + " from " + sender)
      sender ! "hi"
    }
    case _ => println("Received unknown msg ")
  }
}

object RemoteActor{
  def main(args: Array[String]) {

    val system = ActorSystem("RemoteSystem" , ConfigFactory.load("remote_application"))
    //create a remote actor from actorSystem
    val remote = system.actorOf(Props[RemoteActor], name="remote")
    println("remote is ready")

  }
}