package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._

case class Track(id: Long, name: String, duration: String, location: String, albumid: Long)

object Tracks {
  val parser = (
    get[Long]("id") ~
    get[String]("name") ~
    get[String]("duration") ~
    get[String]("location") ~
    get[Long]("album_id") map {
      case id ~ name ~ duration ~ location ~ albumid => Track(id, name, duration, location, albumid)
    })

  implicit val writer = Json.writes[Track]

  def byAlbum(albumid: Long): List[Track] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT id, name, duration, location, album_id FROM tracks WHERE album_id = {albumid} ORDER BY id").on('albumid -> albumid).as(Tracks.parser *)
    }
  }
  
  def byId(id: Long): Track = {
    DB.withConnection { implicit connection =>
      SQL("SELECT id, name, duration, location, album_id FROM tracks WHERE id = {id}").on('id -> id).single(Tracks.parser)
    }
  }
}