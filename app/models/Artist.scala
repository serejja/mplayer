package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._
import service.Release
import java.io.File

case class Artist(id: Long, name: String, genreid: Long)

object Artists {
  val parser = (
    get[Long]("id") ~
    get[String]("name") ~
    get[Long]("genre_id") map {
      case id ~ name ~ genreid => Artist(id, name, genreid)
    })

  implicit val writer = Json.writes[Artist]

  def byId(artistid: Long): Artist = {
    val artists = DB.withConnection { implicit connection =>
      SQL("SELECT id, name, genre_id " +
        " FROM artists " +
        " WHERE id = {id}").on('id -> artistid).as(Artists.parser *)
    }
    artists.head
  }

  def byGenre(genreid: Long): List[Artist] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT id, name, genre_id " +
        " FROM artists " +
        " WHERE genre_id = {genreid} " +
        " ORDER BY name").on('genreid -> genreid).as(Artists.parser *)
    }
  }

  def getOrNew(genreid: Long, name: String): Artist = {
    var artists = DB.withConnection { implicit connection =>
      SQL("SELECT id, name, genre_id " +
        " FROM artists " +
        " WHERE genre_id = {genreid} AND lower(name) = {name}").on('genreid -> genreid, 'name -> name.toLowerCase).as(Artists.parser *)
    }
    if (artists.isEmpty) {
      artists = DB.withConnection { implicit connection =>
        SQL("INSERT INTO artists (name, genre_id) " +
          " VALUES ({name}, {genreid}) " +
          " RETURNING id, name, genre_id").on('genreid -> genreid, 'name -> name).as(Artists.parser *)
      }
    }
    artists.head
  }

  def delete(artistid: Long) {
    val artist = Artists.byId(artistid)
    val genre = Genres.byId(artist.genreid)
    Albums.byArtist(artistid).foreach(album => Albums.delete(album.id))
    new File(Release.artistLocation(genre, artist)).delete
    DB.withConnection { implicit connection =>
      SQL("DELETE FROM artists " +
        " WHERE id = {id} ").on('id -> artistid).execute
    }
  }
}