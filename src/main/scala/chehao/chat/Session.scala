package chehao.chat

import akka.actor.ActorRef
import akka.actor.Actor
import akka.event.Logging


class Session(user: String, storage: ActorRef) extends Actor {
  private val loginTime = System.currentTimeMillis
  private var userLog: List[String] = Nil
  val log = Logging(context.system, this)
  log.info("New session for user [%s] has been created at [%s]".format(user, loginTime))
  
  def receive: Actor.Receive = {
    case msg @ ChatMessage(from, message) =>
      userLog ::= message
      storage ! msg

    case msg @ GetChatLog(_) =>
      storage forward msg
  }
}