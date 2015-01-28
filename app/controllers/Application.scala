package controllers

import play.api.libs.concurrent.Promise
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WS
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models._
import play.api.Play.current
import services.ChatRoom

import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  implicit val cardFormat = Json.format[Card]

  def index = Action { request =>
    request.session.get("username").map {user =>
      Ok(views.html.index(user))
    }.getOrElse {
      Unauthorized(views.html.index(""))
    }
  }

  def login = Action { implicit request =>
    val (username, password) = userLoginForm.bindFromRequest.get
    val user = User(username = username, password = password)
    Users.authenticate(user) match {
      case Nil =>
        Unauthorized(views.html.index(""))
      case u::Nil  =>
        Redirect(routes.Application.index()).withSession(
          "userId" -> u.id.get.toString,
          "username" -> u.username
        )
    }
  }

  def logout = Action {
    Redirect(routes.Application.index()).withNewSession
  }

  def flashCard = Action { request =>
    request.session.get("username").map {user =>
      Ok(views.html.flashcard(user))
    }.getOrElse {
      Unauthorized(views.html.index(""))
    }
  }

  def chat = Action { request =>
    request.session.get("username").map {user =>
      Ok(views.html.chat(user))
    }.getOrElse {
      Unauthorized(views.html.index(""))
    }
  }

  def sendChat(username: String) = WebSocket.acceptWithActor[String, JsValue] { request => out =>
    ChatRoom.props(username, out)
  }

  val cardForm = Form {
    tuple(
      "word" -> text,
      "read" -> text,
      "mean" -> text
    )
  }

  val userLoginForm = Form {
    tuple (
      "username" -> text,
      "password" -> text
    )
  }

  def getCards = Action { request =>
    val userId = request.session.get("userId").get.toInt
    val cards = Cards.getCardsByUserId(userId)
    Ok(Json.toJson(cards))
  }

  def addCard = Action { implicit request =>
    val userId = request.session.get("userId").get.toInt
    val (word, read, mean) = cardForm.bindFromRequest.get
    val card = Card(word = word, read = read, mean = mean, user_id = userId)
    Cards.saveCard(card)
    Redirect(routes.Application.flashCard())
  }



  def test = Action.async {
    val futureResponse = WS.url("http://api.openweathermap.org/data/2.5/weather?q=London,uk").get
    futureResponse.map { response =>
      Ok(views.html.test(response.body))
    }
  }

}