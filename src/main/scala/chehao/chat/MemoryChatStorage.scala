package chehao.chat

import akka.actor.Actor
import akka.event.Logging


/**
 * Abstraction of chat storage holding the chat log.
 */
trait ChatStorage extends Actor

class MemoryChatStorage extends ChatStorage {
  //self.lifeCycle = Permanent
 
  private var chatLog : List[String] = List()
  
  val log = Logging(context.system, this)
  log.info("Memory-based chat storage is starting up...")
 
  def receive = {
    case msg @ ChatMessage(from, message) =>      
      log.info("New chat message [%s] from sender : %s".format(message, sender()))
      chatLog ::= message
      val messageList = chatLog
      sender()!ChatLog(messageList)
      //atomic { chatLog + message.getBytes("UTF-8") }
 
    case GetChatLog(_) =>
     
      val messageList = chatLog
      sender()!ChatLog(messageList)
  }
 
  override def postRestart(reason: Throwable) = chatLog = List()
}