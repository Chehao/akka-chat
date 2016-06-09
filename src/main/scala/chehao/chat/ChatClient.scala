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

class Sender extends Actor {

  val chat = context.actorSelection("akka.tcp://RemoteSystem@127.0.0.1:2552/user/server")

  def receive: Actor.Receive = {
    case msg: GetChatLog => chat forward msg
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
    val system = ActorSystem("ClientSystem", ConfigFactory.load("client"))
    val client = system.actorOf(Props(classOf[Sender]), "clientActor")
    var name = args(0)
    var running = true;
    print("\u001b\u0063")
    client ! Login(name)
    while (running) {

      println("")
      print(name + " >")
      val message = scala.io.StdIn.readLine()

      message match {
        case "exit" | "Logout" => {
          client ! Logout(name)
          running = false
        }
        case _ => {
          //userLog ::= message
          client ! ChatMessage(name,name + ": " + message)
          val future = (client ? GetChatLog(name))
          val result = Await.result(future, timeout.duration).asInstanceOf[ChatLog]
          //println("result msg count :"+result.log.length)
          print("\u001b\u0063")
          result.log.reverse.foreach { x => println(x) }

        }
      }
    }
    println("bye bye " + name)
    system.terminate()

  }
}
 