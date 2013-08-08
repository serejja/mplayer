package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._
import service.Release
import java.io.File

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

  def byId(albumid: Long): Album = {
    val albums = DB.withConnection { implicit connection =>
      SQL("SELECT id, name, issue_year, format, artist_id " +
        " FROM albums " +
        " WHERE id = {id} ").on('id -> albumid).as(Albums.parser *)
    }
    albums.head
  }

  def byArtist(artistid: Long): List[Album] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT id, name, issue_year, format, artist_id " +
        " FROM albums " +
        " WHERE artist_id = {artistid} " +
        " ORDER BY issue_year").on('artistid -> artistid).as(Albums.parser *)
    }
  }

  def getOrNew(artist: Artist, name: String, year: Long, format: String): Album = {
    var albums = DB.withConnection { implicit connection =>
      SQL("SELECT id, name, issue_year, format, artist_id " +
        " FROM albums " +
        " WHERE artist_id = {artistid} AND lower(name) = {name} AND issue_year = {year}").on('artistid -> artist.id, 'name -> name.toLowerCase, 'year -> year).as(Albums.parser *)
    }
    if (albums.isEmpty) {
      albums = DB.withConnection { implicit connection =>
        SQL("INSERT INTO albums (name, issue_year, format, artist_id) " +
          " VALUES ({name}, {year}, {format}, {artistid}) " +
          " RETURNING id, name, issue_year, format, artist_id").on('name -> name, 'year -> year, 'format -> format, 'artistid -> artist.id).as(Albums.parser *)
      }
    }
    albums.head
  }

  def delete(albumid: Long) {
    val album = byId(albumid)
    val artist = Artists.byId(album.artistid)
    val genre = Genres.byId(artist.genreid)
    Tracks.byAlbum(albumid).foreach(track => Tracks.delete(track.id))
    new File(Release.albumLocation(genre, artist, album)).delete
    DB.withConnection { implicit connection =>
      SQL("DELETE FROM albums " +
        " WHERE id = {id} ").on('id -> albumid).execute
    }
  }
}