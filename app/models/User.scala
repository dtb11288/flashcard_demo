package models

import java.security.MessageDigest

import play.api.db.DB

import scala.slick.driver.MySQLDriver.simple._
import play.api.Play.current
import StringUtils._

case class User(id: Option[Int] = None, username: String, password: String)

class Users(tag: Tag)
  extends Table[User](tag, "USER") {
  def id: Column[Int] = column[Int]("USER_ID", O.PrimaryKey, O.AutoInc)
  def username: Column[String] = column[String]("USER_USERNAME", O.NotNull)
  def password: Column[String] = column[String]("USER_PASSWORD", O.NotNull)

  def * = (id.?, username, password) <> (User.tupled, User.unapply)
}

object Users {
  val users = TableQuery[Users]
  lazy val db = Database.forDataSource(DB.getDataSource())

//  def getUser(username: String) = {
//    db.withSession { implicit session =>
//      users.list.filter(_.username == username)
//    }
//  }

  def authenticate(user: User) = {
    val query = for (u <- users if u.username === user.username && u.password === user.password.md5) yield u
    db.withSession {implicit session =>
      query.list
    }
  }

  def getUsers = {
    db.withSession { implicit session =>
      users.list
    }
  }

  def saveUser(user: User) = {
    db.withSession {implicit session =>
      val md5User = User(user.id, user.username, user.password.md5)
      users.insert(md5User)
    }
  }
}

object StringUtils {
  implicit class Md5String(val str: String) {
    def md5 = {
      val md = MessageDigest.getInstance("MD5")
      md.digest(str.getBytes).map("%02X".format(_)).mkString
    }
  }
}