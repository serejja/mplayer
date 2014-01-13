package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._
import service.Release
import java.io.File
import models.Genres.writer
import play.api.data._
import play.api.data.Forms._

case class Artist(id: Long, name: String, genre: Genre, country: Country) {
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
    Genres.parser ~
    Countries.parser map {
      case id ~ name ~ genre ~ country => Artist(id, name, genre, country)
    })

  val optionParser = (
    get[Long]("artist$id") ~
    get[String]("artist$name") map {
      case id ~ name => (id.toString -> name)
    })

  implicit val writer = new Writes[Artist] {
    def writes(c: Artist): JsValue = {
      Json.obj("id" -> c.id,
        "name" -> c.name,
        "genre" -> c.genre.toJson,
        "country" -> c.country.toJson)
    }
  }

  val requestForm = Form(
    mapping("id" -> longNumber,
      "name" -> nonEmptyText,
      "genre" -> longNumber,
      "country" -> longNumber)(
        (id, name, genre, country) => Artist(id, name, Genres.byId(genre), Countries.byId(country)))(
          (artist: Artist) => Some(artist.id, artist.name, artist.genre.id, artist.country.id)))

  def comboOptions: Seq[(String, String)] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT id AS artist$id, name AS artist$name" +
        " FROM artists").as(optionParser *)
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
      SQL("SELECT " + columns +
        from +
        " WHERE a.genre_id = {genreid} " +
        " ORDER BY a.name").on('genreid -> genreid).as(Artists.parser *)
    }
  }

  def byCountry(countryid: Long): List[Artist] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT " + columns +
        from +
        " WHERE c.id = {countryid} " +
        " ORDER BY a.name").on('countryid -> countryid).as(Artists.parser *)
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

  def byName(name: String): Artist = {
    DB.withConnection { implicit connection =>
      SQL("SELECT " + columns +
        from +
        " WHERE lower(a.name) = {name}").on('name -> name.toLowerCase).as(Artists.parser *).head
    }
  }

  def create(genreid: Long, name: String, countryid: Long): Artist = {
    DB.withConnection { implicit c =>
      byId(SQL("INSERT INTO artists (name, genre_id, country_id) " +
        " VALUES ({name}, {genreid}, {countryid}) " +
        " RETURNING id").on('genreid -> genreid, 'name -> name, 'countryid -> countryid).single(get[Long]("id")))
    }
  }

  def update(artist: Artist) {
    DB.withConnection { implicit connection =>
      SQL("UPDATE artists " +
        " SET name = {name}, genre_id = {genre}, country_id = {country} " +
        " WHERE id = {id}").on('id -> artist.id, 'name -> artist.name, 'genre -> artist.genre.id, 'country -> artist.country.id).execute
    }
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