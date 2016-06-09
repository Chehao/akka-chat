package chehao.chat

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.actor.Props
import scala.collection.mutable.HashMap
import akka.event.Logging
import scala.collection.mutable.HashSet

object ChatServer {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("RemoteSystem", ConfigFactory.load("server"))
    val actor = system.actorOf(Props[ChatServer], "server")
    
  }
}

class ChatServer extends Actor {

  val storage: ActorRef = context.actorOf(Props[MemoryChatStorage], "storage")
  val sessions: HashMap[String, ActorRef] = new HashMap[String, ActorRef]
  //val senders: HashSet[ActorRef] = new HashSet[ActorRef]
  //self.faultHandler = OneForOneStrategy(List(classOf[Exception]),5, 5000)
  val log = Logging(context.system, this)
  log.info("ChatServer is starting up...")
  
  
  def receive = sessionManagement orElse chatManagement

  protected def sessionManagement: Receive = {
    case Login(username) =>
      println(this, "User [%s] has logged in".format(username))
      val session = context.actorOf(Props(classOf[Session], username, storage))
      //context.watch(session)
      sessions += (username -> session)
      //senders +=(sender())
    case Logout(username) =>
      val session = sessions(username)
      context.stop(session)
      sessions.-=(username)
      println(this, "User [%s] has logout ".format(username))
  }

  protected def chatManagement: Receive = {
    case msg @ ChatMessage(from, _) => {
      getSession(from).foreach(_ ! msg)
      getSession(from).foreach(_ forward GetChatLog(from))
      //sender() ! ChatLog (List(msg.message))      
    }
    case msg @ GetChatLog(from) => getSession(from).foreach(_ forward msg)
  }

  private def getSession(from: String): Option[ActorRef] = {
    if (sessions.contains(from))
      Some(sessions(from))
    else {
      print(this, "Session expired for %s".format(from))
      None
    }
  }

}
 