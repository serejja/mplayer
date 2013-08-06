package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._

case class Artist(id: Long, name: String, genreid: Long)

object Artists {
  val parser = (
    get[Long]("id") ~
    get[String]("name") ~
    get[Long]("genre_id") map {
      case id ~ name ~ genreid => Artist(id, name, genreid)
    })

  implicit val writer = Json.writes[Artist]

  def byGenre(genreid: Long): List[Artist] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT id, name, genre_id FROM artists WHERE genre_id = {genreid}").on('genreid -> genreid).as(Artists.parser *)
    }
  }
}