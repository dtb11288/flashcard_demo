package models

import play.api.db.DB

import scala.slick.driver.MySQLDriver.simple._
import play.api.Play.current

case class Card(id: Option[Int] = None, word: String, read: String, mean: String, user_id: Int)

class Cards(tag: Tag)
  extends Table[Card](tag, "CARD") {
  def id: Column[Int] = column[Int]("CARD_ID", O.PrimaryKey, O.AutoInc)
  def word: Column[String] = column[String]("CARD_WORD", O.NotNull)
  def read: Column[String] = column[String]("CARD_READ", O.NotNull)
  def mean: Column[String] = column[String]("CARD_MEAN", O.NotNull)
  def userId: Column[Int] = column[Int]("CARD_USER_ID", O.NotNull)

  def * = (id.?, word, read, mean, userId) <> (Card.tupled, Card.unapply)
}

object Cards {
  val cards = TableQuery[Cards]
  lazy val db = Database.forDataSource(DB.getDataSource())

  def getCards = {
    db.withSession {implicit session =>
      cards.list
    }
  }

  def getCardsByUserId(userId: Int) = {
    val query = for (c <- cards if c.userId === userId) yield c
    db.withSession {implicit session =>
      query.list
    }
  }

  def saveCard(card: Card) = {
    db.withSession {implicit session =>
      cards.insert(card)
    }
  }
}