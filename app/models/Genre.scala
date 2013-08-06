package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._

case class Genre(id: Long, name: String)

object Genres {
  val parser = (
    get[Long]("id") ~
    get[String]("name") map {
      case id ~ name => Genre(id, name)
    })

  implicit val writer = Json.writes[Genre]

  def all: List[Genre] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT id, name FROM genres").as(Genres.parser *)
    }
  }
}