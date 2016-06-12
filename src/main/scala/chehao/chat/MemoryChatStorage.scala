package chehao.chat

import akka.actor.Actor
import akka.event.Logging
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

/**
 * Abstraction of chat storage holding the chat log.
 */
trait ChatStorage extends Actor

class MemoryChatStorage extends ChatStorage {
  //self.lifeCycle = Permanent

  private var chatLog: ListBuffer[String] = ListBuffer("< Room >")
  val friends: HashMap[String, ListBuffer[String]] = HashMap[String, ListBuffer[String]]()
  private val chatMessageStorage: HashMap[String, HashMap[String, ListBuffer[String]]] = HashMap[String, HashMap[String, ListBuffer[String]]]()

  val log = Logging(context.system, this)
  log.info("Memory-based chat storage is starting up...")

  def receive = {
    case msg @ ChatMessage(from, message) =>
      log.info("New chat message [%s] from sender : %s".format(message, sender()))
      chatLog += message
      if (chatLog.size >= 10) {
        val messageList = chatLog.slice(chatLog.size - 10, chatLog.size)
        sender() ! ChatLog(messageList)
      } else {
        val messageList = chatLog
        sender() ! ChatLog(messageList)
      }

    case msg @ ChatMessageTo(from, to, message) =>
      log.info("New chat message (%s->%s) [%s] from sender : %s".format(from, to, message, sender()))
      val fromUserMap = chatMessageStorage.getOrElseUpdate(from, HashMap[String, ListBuffer[String]]())
      val msgs = fromUserMap.getOrElseUpdate(to, ListBuffer[String]("< " + to + " >")) += message
      //add message to target MessabBox map
      val toUserMap = chatMessageStorage.getOrElseUpdate(to, HashMap[String, ListBuffer[String]]())
      val msgTo = toUserMap.getOrElseUpdate(from, ListBuffer[String]("< " + from + " >")) += message

      sender() ! ChatLog(msgs)

    case msg @ GetChatMessageTo(from, to) =>
      val fromUserMap = chatMessageStorage.getOrElseUpdate(from, HashMap[String, ListBuffer[String]]())
      val msgs = fromUserMap.getOrElseUpdate(to, ListBuffer[String]("< " + to + " >"))
      sender() ! ChatLog(msgs)

    case GetChatLog(_) =>
      val messageList = chatLog
      sender() ! ChatLog(messageList)

    case msg @ AddFriend(user, friend) =>
      log.info("AddFriend message %s->%s from sender : %s".format(user, friend, sender()))
      val friendList = friends.getOrElseUpdate(user, ListBuffer[String]("<Friend List>")) += friend
      sender() ! ChatLog(friendList)

  }

  override def postRestart(reason: Throwable) = chatLog = ListBuffer()
}