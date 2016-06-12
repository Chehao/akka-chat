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
  var toWhom  = ""
  
  def receive: Actor.Receive = {

    case msg: ChatLog => { //back message
      print("\u001b\u0063")
      msg.log.foreach { x => println(x) }
      print(username + ">" + toWhom + " ")
    }
    case msg: ChatMessage => {
      if (toWhom.isEmpty())
        chat ! ChatMessage(username, msg.message)
      else
        chat ! ChatMessageTo(username, toWhom, msg.message)
    }
    case msg @ ChangeTo(to) =>
      if (to.isEmpty())
        println("change to all")
      else 
        println("change to " + to)
      toWhom = to
      print(username + ">" + toWhom + " ")
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
        case "AddFriend" =>
          println(name + " AddFrient> ")
          val friendToAdd = scala.io.StdIn.readLine()
          client ! AddFriend(name, friendToAdd)
        case "To" =>
          println(name + " To Who?")
          val to = scala.io.StdIn.readLine()
          client ! ChangeTo(to)
        case _ =>
          client ! ChatMessage(name, name + ": " + message)
        /*
           * wait future
          val fu1  = client ? ChatMessage(name,name + ": " + message)
          val result = Await.result(fu1, timeout.duration).asInstanceOf[ChatLog]
          //print("\u001b\u0063")
          result.log.reverse.foreach { x => println(x) }*/

      }
    }
    println("bye bye " + name)

    system.terminate()

  }
}
 