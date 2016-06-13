# akka-chat
the simple chat system developed by akka & scala

## Design
### ChatClient 
the main object of ChatClient
### Sender actor
the sender actor is as proxy to send/received message from ChatServer, and print the message to stdout
### ChatServer
received message from ChatClient, and 
! create/destory login session when Login/Logout
! forward new message to session 
! push back or broadcast messages to client by notifying each session
### Session actor
!session can keep the original client sender actor to send messages back
!call persistence layer actor to store or access data
### Storage actor 
process data with DAO

## run server
<pre> sbt "run-main chehao.chat.ChatServer" </pre>

## run client
<pre>
sbt "run-main chehao.chat.ChatClient [username]"
</pre>
