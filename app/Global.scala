import play.api.db.DB
import play.api.{Logger, GlobalSettings, Application}
import scala.slick.driver.MySQLDriver.simple._
import play.api.Play.current
import models._

import scala.slick.jdbc.meta.MTable

object Global extends GlobalSettings {
  lazy val database = Database.forDataSource(DB.getDataSource())

  override def onStart(app: Application) {
    database.withSession {implicit session =>
      if (MTable.getTables("CARD").list.isEmpty) {
        Cards.cards.ddl.create
      }
      if (MTable.getTables("USER").list.isEmpty) {
        Users.users.ddl.create
        Users.saveUser(User(username = "root", password = "root"))
        (for(i <- 1 to 10) yield User(username = "user" + i.toString, password = "password" + i.toString)).foreach(Users.saveUser)
      }
    }
  }
}
