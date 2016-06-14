# akka-chat - the simple chat system developed by akka & scala

## Description

the project is based on akka 1.3.1 tutorial , but upgrade the akka api version to 2.4.6
and enhance client interaction with server , server can push messages back to each login client .  

http://doc.akka.io/docs/akka/1.3.1/scala/tutorial-chat-server.html

- ChatClient  - the main object 

- Proxy actor - chat client use proxy actor to communicate to ChatServer, and proxy can print received messages from server .
    
- ChatServer -  the chat server, manage logged in session and send messages to all client or special client by session

- Session actor - session can keep the conversation with client , so it keep original client sender actor to send messages back ,
  use  storage actor to store data

- Storage actor - data persistence

## Development
* akka 2.4.6
* scala 2.11.8
* Java 8
* SBT 0.13.11

### Create eclipse project
``` 
sbt ecliopse
```

### run server
``` 
sbt "run-main chehao.chat.ChatServer" 
```

### run client
```
sbt "run-main chehao.chat.ChatClient "
```
## command
* Login - to Login
* Logout - to logout session 
* exit - to exit program
* AddFriend - add to friend list
* To - change to chat user ( if entry empty, then chat to everyone)
