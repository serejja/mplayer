package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._

case class Album(id: Long, name: String, year: Long, format: String, artistid: Long)

object Albums {
  val parser = (
    get[Long]("id") ~
    get[String]("name") ~
    get[Long]("issue_year") ~
    get[String]("format") ~
    get[Long]("artist_id") map {
      case id ~ name ~ year ~ format ~ artistid => Album(id, name, year, format, artistid)
    })

  implicit val writer = Json.writes[Album]

  def byArtist(artistid: Long): List[Album] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT id, name, issue_year, format, artist_id FROM albums WHERE artist_id = {artistid}").on('artistid -> artistid).as(Albums.parser *)
    }
  }
}