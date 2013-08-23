package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._
import service.Release
import java.io.File
import models.Genres.writer

case class Artist(id: Long, name: String, genre: Genre) {
  def toJson: JsValue = {
    Artists.writer.writes(this)
  }
}

object Artists {
  val columns = " a.id AS artist$id, a.name AS artist$name, c.id AS country$id, c.name AS country$name," + Genres.columns
  private val from = " FROM artists a INNER JOIN genres g ON a.genre_id = g.id LEFT JOIN countries c ON a.country_id = c.id "
  
  val parser = (
    get[Long]("artist$id") ~
    get[String]("artist$name") ~
    Genres.parser map {
      case id ~ name ~ genre => Artist(id, name, genre)
    })

  implicit val writer = new Writes[Artist] {
    def writes(c: Artist): JsValue = {
      Json.obj("id" -> c.id,
        "name" -> c.name,
        "genre" -> c.genre.toJson)
    }
  }

  def byId(artistid: Long): Artist = {
    val artists = DB.withConnection { implicit connection =>
      SQL("SELECT " + columns +
        from +
        " WHERE a.id = {id}").on('id -> artistid).as(Artists.parser *)
    }
    artists.head
  }
  
  def byGenre(genreid: Long): List[Artist] = {
    DB.withConnection { implicit connection =>
      System.out.println("SELECT " + columns +
        from +
        " WHERE a.genre_id = {genreid} " +
        " ORDER BY a.name");
      SQL("SELECT " + columns +
        from +
        " WHERE a.genre_id = {genreid} " +
        " ORDER BY a.name").on('genreid -> genreid).as(Artists.parser *)
    }
  }

  def byTrackId(trackid: Long): Artist = {
    DB.withConnection { implicit connection =>
      SQL("SELECT " + columns +
        from +
        " INNER JOIN tracks t ON al.id = t.album_id " +
        " WHERE t.id = {id} " +
        " LIMIT 1").on('id -> trackid).single(Artists.parser)
    }
  }

  def getOrNew(genreid: Long, name: String): Artist = {
    var artists = DB.withConnection { implicit connection =>
      SQL("SELECT " + columns +
        from +
        " WHERE a.genre_id = {genreid} AND lower(a.name) = {name}").on('genreid -> genreid, 'name -> name.toLowerCase).as(Artists.parser *)
    }
    if (artists.isEmpty) {
      val id = DB.withConnection { implicit connection =>
        SQL("INSERT INTO artists (name, genre_id) " +
          " VALUES ({name}, {genreid}) " +
          " RETURNING id").on('genreid -> genreid, 'name -> name).single(get[Long]("id"))
      }
      return byId(id)
    }
    artists.head
  }

  def delete(artistid: Long) {
    val artist = Artists.byId(artistid)
    val genre = Genres.byId(artist.genre.id)
    Albums.byArtist(artistid).foreach(album => Albums.delete(album.id))
    new File(Release.artistLocation(genre, artist)).delete
    DB.withConnection { implicit connection =>
      SQL("DELETE FROM artists " +
        " WHERE id = {id} ").on('id -> artistid).execute
    }
  }
}