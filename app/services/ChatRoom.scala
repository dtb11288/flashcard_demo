package services

import akka.actor._
import play.api.libs.json.Json

import scala.collection.mutable
import ChatRoom.userList

object ChatRoom {
  var userList = mutable.HashMap[ActorRef, String]()
  def props(username: String, out: ActorRef) = Props(new ChatRoom(username, out))
}

class ChatRoom(username: String, out: ActorRef) extends Actor {
  userList(out) = username
  def receive = {
    case msg: String =>
      userList.foreach {
        case (target, name) =>
          target ! Json.toJson(Map("text" -> msg, "username" -> username))
      }
  }
  override def postStop() = {
    userList.remove(out)
  }
}
