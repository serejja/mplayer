package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._
import java.io.File
import service.FileUtils._
import service.Log

case class Track(id: Long, name: String, duration: String, location: String, album: Album) {
  def toJson: JsValue = {
    Tracks.writer.writes(this)
  }
}

case class TrackInfo(artist: String, title: String, duration: String)

object Tracks {
  private val columns = " t.id AS track$id, t.name AS track$name, t.duration AS track$duration, t.location AS track$location, " + Albums.columns
  private val from = " FROM tracks t INNER JOIN albums al ON t.album_id = al.id INNER JOIN artists a on al.artist_id = a.id INNER JOIN genres g on a.genre_id = g.id LEFT JOIN countries c ON a.country_id = c.id "
  
  val parser = (
    get[Long]("track$id") ~
    get[String]("track$name") ~
    get[String]("track$duration") ~
    get[String]("track$location") ~
    Albums.parser map {
      case id ~ name ~ duration ~ location ~ album => Track(id, name, duration, location, album)
    })

  val trackInfoParser = (
    get[String]("artist") ~
    get[String]("title") ~
    get[String]("duration") map {
      case artist ~ title ~ duration => TrackInfo(artist, title, duration)
    })

  implicit val writer = new Writes[Track] {
    def writes(c: Track): JsValue = {
      Json.obj("id" -> c.id,
        "name" -> c.name,
        "duration" -> c.duration,
        "location" -> c.location,
        "album" -> c.album.toJson)
    }
  }
  implicit val trackInfoWriter = Json.writes[TrackInfo]

  def byAlbum(albumid: Long): List[Track] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT " + columns +
        from +
        " WHERE t.album_id = {albumid} " +
        " ORDER BY t.id").on('albumid -> albumid).as(Tracks.parser *)
    }
  }
  
  def byArtist(artistid: Long): List[Track] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT " + columns + 
        from +
        " WHERE a.id = {artistid} " +
        " ORDER BY t.id").on('artistid -> artistid).as(Tracks.parser *)
    }
  }
  
  def byGenre(genreid: Long): List[Track] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT " + columns +
        from +
        " WHERE g.id = {genreid} " +
        " ORDER BY t.id").on('genreid -> genreid).as(Tracks.parser *)
    }
  }

  def byId(id: Long): Track = {
    val tracks = DB.withConnection { implicit connection =>
      SQL("SELECT " + columns +
        from +
        " WHERE t.id = {id}").on('id -> id).as(Tracks.parser *)
    }
    if (tracks.length > 0) tracks.head else null
  }

  def byNameAndAlbum(name: String, albumid: Long): Track = {
    val tracks = DB.withConnection { implicit connection =>
      SQL("SELECT " + columns +
        from +
        " WHERE lower(t.name) = {name} AND album_id = {t.albumid}").on('name -> name.toLowerCase, 'albumid -> albumid).as(Tracks.parser *)
    }
    if (tracks.length > 0) tracks.head else null
  }

  def add(name: String, duration: String, location: String, albumid: Long): Track = {
    if (!exists(name, albumid)) {
      val id = DB.withConnection { implicit connection =>
        SQL("INSERT INTO tracks (name, duration, location, album_id)" +
          " VALUES ({name}, {duration}, {location}, {albumid}) " +
          " RETURNING id").on('name -> name, 'duration -> duration, 'location -> location, 'albumid -> albumid).single(get[Long]("id"))
      }
      return byId(id)
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

  def trackInfo(trackid: Long): TrackInfo = {
    DB.withConnection { implicit connection =>
      SQL("SELECT a.name as artist, t.name as title, t.duration as duration " +
        " FROM tracks t " +
        " INNER JOIN albums al on t.album_id = al.id " +
        " INNER JOIN artists a on al.artist_id = a.id " +
        " WHERE t.id = {id}").on('id -> trackid).single(Tracks.trackInfoParser)
    }
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
    byNameAndAlbum(name, albumid) != null
  }
}