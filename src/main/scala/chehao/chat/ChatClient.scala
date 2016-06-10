package chehao.chat

import akka.actor.ActorSystem
import akka.actor.Identify
import akka.pattern.AskableActorSelection
import akka.actor.Actor
import akka.actor.Props
import com.typesafe.config.ConfigFactory
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.Await

class Sender(username: String) extends Actor {

  val chat = context.actorSelection("akka.tcp://RemoteSystem@127.0.0.1:2552/user/server")

  def receive: Actor.Receive = {

    case msg: GetChatLog => chat forward msg
    case msg: ChatMessage => chat forward msg
    case msg: Login => chat ! msg
    case msg: ChatLog => {
      print("\u001b\u0063")
      msg.log.reverse.foreach { x => println(x) }
      print(username + "> ")
    }
    case msg: Event => chat ! msg
    case _ => println("unknow message")
  }

}

object ChatClient {

  def main(args: Array[String]): Unit = {
    implicit val timeout = Timeout(5 seconds)
    var userLog: List[String] = Nil
    if (args.length < 1) {
      println("ChatClient [user name]")
      System.exit(0)
    }
    var name = args(0)
    val system = ActorSystem("ClientSystem", ConfigFactory.load("client"))
    val client = system.actorOf(Props(classOf[Sender], name), "clientActor")
    var running = true;

    client ! Login(name)
    
    while (running) {

      val message = scala.io.StdIn.readLine()
      message match {
        case "exit" | "Logout" => {
          client ! Logout(name)
          running = false
        }
        case _ => {
          client ! ChatMessage(name, name + ": " + message)
          /*
           * wait future
          val fu1  = client ? ChatMessage(name,name + ": " + message)
          val result = Await.result(fu1, timeout.duration).asInstanceOf[ChatLog]
          //print("\u001b\u0063")
          result.log.reverse.foreach { x => println(x) }*/
        }
      }
    }
    println("bye bye " + name)

    system.terminate()

  }
}
 