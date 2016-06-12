package chehao.chat

import akka.actor.ActorRef
import akka.actor.Actor
import akka.event.Logging

class Session(user: String, fromClient: ActorRef, storage: ActorRef) extends Actor {
  private val loginTime = System.currentTimeMillis
  private var userLog: List[String] = Nil
  val log = Logging(context.system, this)
  log.info("New session for user [%s] has been created at [%s]".format(user, loginTime))

  def receive: Actor.Receive = {
    case msg @ ChatMessage(from, message) =>
      userLog ::= message
      storage forward msg //forward server to storage

    case msg @ GetChatLog(_) =>
      storage forward msg
    
    case msg @ AddFriend(user,friend) =>
      log.info("session keep client is %s".format(fromClient))
      log.info("session from %s".format(sender()))
      storage ! msg
    
    case msg @ ChatMessageTo(from,to,message)=>
      storage ! msg
    case msg @ GetChatMessageTo(from,to)=>
      storage ! msg  
      
    case msg : ChatLog =>
      log.info("session keep client is %s".format(fromClient))
      log.info("rev friends message");
      fromClient ! msg
  }
}