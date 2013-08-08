package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._
import java.io.File
import service.FileUtils._

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
      SQL("SELECT id, name, duration, location, album_id " +
        " FROM tracks " +
        " WHERE album_id = {albumid} " +
        " ORDER BY id").on('albumid -> albumid).as(Tracks.parser *)
    }
  }

  def byId(id: Long): Track = {
    val tracks = DB.withConnection { implicit connection =>
      SQL("SELECT id, name, duration, location, album_id " +
        " FROM tracks " +
        " WHERE id = {id}").on('id -> id).as(Tracks.parser *)
    }
    if (tracks.length > 0) tracks.head else null
  }

  def byNameAndAlbum(name: String, albumid: Long): Track = {
    val tracks = DB.withConnection { implicit connection =>
      SQL("SELECT id, name, duration, location, album_id " +
        " FROM tracks " +
        " WHERE lower(name) = {name} AND album_id = {albumid}").on('name -> name.toLowerCase, 'albumid -> albumid).as(Tracks.parser *)
    }
    if (tracks.length > 0) tracks.head else null
  }

  def add(name: String, duration: String, location: String, albumid: Long): Track = {
    if (!exists(name, albumid)) {
	    return DB.withConnection { implicit connection =>
	      SQL("INSERT INTO tracks (name, duration, location, album_id)" +
	        " VALUES ({name}, {duration}, {location}, {albumid}) " +
	        " RETURNING id, name, duration, location, album_id").on('name -> name, 'duration -> duration, 'location -> location, 'albumid -> albumid).single(Tracks.parser)
	    }
    }
    null
  }
  
  def delete(trackid: Long) {
    val track = byId(trackid)
    new File(track.location).delete
    DB.withConnection { implicit connection =>
      SQL("DELETE FROM tracks " +
        " WHERE id = {id}").on('id -> trackid).execute
    }
  }

  def formatSupported(file: File): Boolean = {
    return formatExtensionSupported(extension(file))
  }

  def parseDuration(seconds: Long): String = {
    f"${seconds / 60}%d:${seconds % 60}%02d"
  }

  private def formatExtensionSupported(extension: String): Boolean = {
    val count = DB.withConnection { implicit connection =>
      SQL("SELECT COUNT(*) as cnt " +
        " FROM supported_formats " +
        " WHERE lower(name) = {name}").on('name -> extension.toLowerCase).single(get[Long]("cnt"))
    }
    return count > 0
  }

  private def exists(name: String, albumid: Long): Boolean = {
    byNameAndAlbum(name, albumid) == null
  }
}