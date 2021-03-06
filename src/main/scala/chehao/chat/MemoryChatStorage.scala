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

  private val chatLog: ListBuffer[String] = ListBuffer()
  val friends: HashMap[String, ListBuffer[String]] = HashMap[String, ListBuffer[String]]()
  private val chatMessageStorage: HashMap[String, HashMap[String, ListBuffer[String]]] = HashMap[String, HashMap[String, ListBuffer[String]]]()

  val log = Logging(context.system, this)
  log.info("Memory-based chat storage is starting up...")

  def receive = {
    case msg @ ChatMessage(from, message) =>
      log.info("New chat message [%s] from sender : %s".format(message, sender()))
      chatLog += message
      
      val messageList = ListBuffer("< Room >") ++ chatLog.takeRight(10)
      sender() ! ChatLog(messageList)
       

    case msg @ ChatMessageTo(from, to, message) =>
      log.info("New chat message (%s->%s) [%s] from sender : %s".format(from, to, message, sender()))
      val fromUserMap = chatMessageStorage.getOrElseUpdate(from, HashMap[String, ListBuffer[String]]())
      val msgs = fromUserMap.getOrElseUpdate(to, ListBuffer[String]()) += message
      //add message to target MessabBox map
      val toUserMap = chatMessageStorage.getOrElseUpdate(to, HashMap[String, ListBuffer[String]]())
      val msgTo = toUserMap.getOrElseUpdate(from, ListBuffer[String]()) += message
      //val messageList = ListBuffer("< " + to + " >") ++ msgs.takeRight(10)
      //sender() ! ChatLog(msgs)
      sender() ! ChatMessageToOk(from,to)

    case msg @ GetChatMessageTo(from, to) =>
      val fromUserMap = chatMessageStorage.getOrElseUpdate(from, HashMap[String, ListBuffer[String]]())
      val msgs = fromUserMap.getOrElseUpdate(to, ListBuffer[String]())
      val messageList = ListBuffer("< " + to + " >") ++ msgs.takeRight(10)
      sender() ! ChatLog(messageList)

    case GetChatLog(_) =>
      val messageList = chatLog
      sender() ! ChatLog(messageList)

    case msg @ AddFriend(user, friend) =>
      log.info("AddFriend message %s->%s from sender : %s".format(user, friend, sender()))
      val friendList = friends.getOrElseUpdate(user, ListBuffer[String]("<Friend List>")) += friend
      sender() ! ChatLog(friendList)

  }

  //override def postRestart(reason: Throwable) = chatLog = ListBuffer()
}