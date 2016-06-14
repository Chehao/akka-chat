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

class Proxy() extends Actor {

  val chat = context.actorSelection("akka.tcp://RemoteSystem@127.0.0.1:2552/user/server")
  var toWhom = ""
  var username = ""

  def receive: Actor.Receive = {
    case msg @ Login(name) =>
      username = name
      chat ! msg
    case msg: Logout =>
      chat ! Logout(username)

    case msg @ AddFriend(user, friend) =>
      chat ! AddFriend(username, friend)

    case msg: ChatLog => { //back message
      print("\u001b\u0063")
      msg.log.foreach { x => println(x) }
      print(username + ">")
    }
    case msg: ChatMessage => {
      if (toWhom.isEmpty())
        chat ! ChatMessage(username, username + ": " + msg.message)
      else
        chat ! ChatMessageTo(username, toWhom, username + ": " + msg.message)
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
    /*if (args.length < 1) {
      println("ChatClient [user name]")
      System.exit(0)
    }*/
    //    

    val system = ActorSystem("ClientSystem", ConfigFactory.load("client"))
    val client = system.actorOf(Props(classOf[Proxy]), "clientActor")
    var running = true;
    var name = ""
    println("\u001b\u0063")

    while (running) {
      if (name.isEmpty()) {
        print("Login :")
        val input = scala.io.StdIn.readLine()
        name = input
        client ! Login(name)
      } else {
        val message = scala.io.StdIn.readLine()
        message match {
          case "exit" => 
            running = false
            if(!name.isEmpty()){
              client ! Logout(name)
              name = ""
            }
          case "Logout" => 
            client ! Logout(name)
            name = ""
          case "AddFriend" =>
            println(" AddFrient> ")
            val friendToAdd = scala.io.StdIn.readLine()
            client ! AddFriend("", friendToAdd)
          case "To" =>
            println(" To Who?")
            val to = scala.io.StdIn.readLine()
            client ! ChangeTo(to)
          case _ =>
            client ! ChatMessage("", message)
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
 