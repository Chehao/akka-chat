package chehao.chat

import scala.collection.mutable.ListBuffer

sealed trait Event {

}
case class Login(user: String) extends Event
case class Logout(user: String) extends Event
case class GetChatLog(from: String) extends Event
case class ChatLog(log: ListBuffer[String]) extends Event
case class ChatMessage(from: String, message: String) extends Event
case class ChatMessageTo(from: String, to: String, message: String) extends Event
case class GetChatMessageTo(from:String,to:String) extends Event
case class AddFriend(user: String, friend: String) extends Event
case class ChangeTo(to: String) extends Event