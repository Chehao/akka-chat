package chehao.chat

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.actor.Props
import scala.collection.mutable.HashMap
import akka.event.Logging
import scala.collection.mutable.HashSet
import scala.collection.mutable.ListBuffer

object ChatServer {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("RemoteSystem", ConfigFactory.load("server"))
    val actor = system.actorOf(Props[ChatServer], "server")

  }
}

class ChatServer extends Actor {

  val storage: ActorRef = context.actorOf(Props[MemoryChatStorage], "storage")
  val sessions: HashMap[String, ActorRef] = new HashMap[String, ActorRef]
  val roomClient: HashSet[ActorRef] = new HashSet[ActorRef]
  //self.faultHandler = OneForOneStrategy(List(classOf[Exception]),5, 5000)
  val log = Logging(context.system, this)
  log.info("ChatServer is starting up...")

  def receive = sessionManagement orElse chatManagement

  protected def sessionManagement: Receive = {
    case Login(username) =>
      log.info("User [%s] has logged in".format(username))
      val session = context.actorOf(Props(classOf[Session], username,sender(), storage))
      sessions += (username -> session)
      roomClient += (sender())
      
      //back message 
      log.info("from %s".format(sender()))
      //sender() ! ChatLog(ListBuffer(username + ": has logged in"))
      getSession(username).foreach(_ ! ChatMessage(username,username + " has logged in")) 
    case Logout(username) =>
      val session = sessions(username)
      context.stop(session)
      sessions-=(username)
      println(this, "User [%s] has logout ".format(username))
  }

  protected def chatManagement: Receive = {
    case msg @ ChatMessage(from, _) => getSession(from).foreach(_ ! msg) 
    case msg @ ChatMessageTo(from,to, _) => 
      getSession(from).foreach(_ ! msg)
    case msg @ ChatMessageToOk(from,to)=>
      getSession(from).foreach(_!GetChatMessageTo(from,to))
      getSession(to).foreach(_!GetChatMessageTo(to,from))
    case msg @ GetChatLog(from) => getSession(from).foreach(_ forward msg)
    case msg @ AddFriend(user,friend) => getSession(user).foreach { _ ! msg } 
    case msg: ChatLog => roomClient.foreach { x => x ! msg }
    case _ => log.info("Unknow message form %s".format(sender()))
  }

  private def getSession(from: String): Option[ActorRef] = {
    if (sessions.contains(from))
      Some(sessions(from))
    else {
      log.info("Session expired for %s".format(from))
      None
    }
  }

}
 