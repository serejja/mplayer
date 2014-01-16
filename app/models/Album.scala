package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._
import service.Release
import java.io.File

case class Album(id: Long, name: String, year: Long, format: String, artist: Artist) {
  def toJson: JsValue = {
    Albums.writer.writes(this)
  }
}

object Albums {
  val columns = " al.id AS album$id, al.name AS album$name, al.issue_year AS album$issue_year, al.format AS album$format, " + Artists.columns
  private val from = " FROM albums al INNER JOIN artists a on al.artist_id = a.id INNER JOIN genres g on a.genre_id = g.id LEFT JOIN countries c ON a.country_id = c.id "

  val parser = (
    get[Long]("album$id") ~
    get[String]("album$name") ~
    get[Long]("album$issue_year") ~
    get[String]("album$format") ~
    Artists.parser map {
      case id ~ name ~ year ~ format ~ artistid => Album(id, name, year, format, artistid)
    })

  implicit val writer = new Writes[Album] {
    def writes(c: Album): JsValue = {
      Json.obj("id" -> c.id,
        "name" -> c.name,
        "year" -> c.year,
        "format" -> c.format,
        "artist" -> c.artist.toJson)
    }
  }

  def byId(albumid: Long): Album = {
    val albums = DB.withConnection { implicit connection =>
      SQL("SELECT " + columns +
        from +
        " WHERE al.id = {id} ").on('id -> albumid).as(Albums.parser *)
    }
    albums.head
  }

  def byArtist(artistid: Long): List[Album] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT " + columns +
        from +
        " WHERE al.artist_id = {artistid} " +
        " ORDER BY al.issue_year").on('artistid -> artistid).as(Albums.parser *)
    }
  }

  def recent(): List[Album] = {
    DB.withConnection { implicit c =>
      SQL("SELECT " + columns +
        from +
        " ORDER BY al.id DESC limit 15").as(Albums.parser *)
    }
  }

  def getOrNew(artist: Artist, name: String, year: Long, format: String): Album = {
    var albums = DB.withConnection { implicit connection =>
      SQL("SELECT " + columns +
        from +
        " WHERE al.artist_id = {artistid} AND lower(al.name) = {name} AND al.issue_year = {year}").on('artistid -> artist.id, 'name -> name.toLowerCase, 'year -> year).as(Albums.parser *)
    }
    if (albums.isEmpty) {
      val id = DB.withConnection { implicit connection =>
        SQL("INSERT INTO albums (name, issue_year, format, artist_id) " +
          " VALUES ({name}, {year}, {format}, {artistid}) " +
          " RETURNING id").on('name -> name, 'year -> year, 'format -> format, 'artistid -> artist.id).single(get[Long]("id"))
      }
      return byId(id)
    }
    albums.head
  }

  def delete(albumid: Long) {
    val album = byId(albumid)
    val artist = Artists.byId(album.artist.id)
    val genre = Genres.byId(artist.genre.id)
    Tracks.byAlbum(albumid).foreach(track => Tracks.delete(track.id))
    new File(Release.albumLocation(genre, artist, album)).delete
    DB.withConnection { implicit connection =>
      SQL("DELETE FROM albums " +
        " WHERE id = {id} ").on('id -> albumid).execute
    }
  }

  def getFormats: Seq[(String, String)] = {
    Seq("LP" -> "LP", "EP" -> "EP", "Single" -> "Single")
  }
}