package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._
import java.io.File
import service.Release

case class Genre(id: Long, name: String)

object Genres {
  val parser = (
    get[Long]("id") ~
    get[String]("name") map {
      case id ~ name => Genre(id, name)
    })

  implicit val writer = Json.writes[Genre]

  def all: List[Genre] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT id, name " +
        " FROM genres " +
        " ORDER BY name").as(Genres.parser *)
    }
  }

  def byId(genreid: Long): Genre = {
    DB.withConnection { implicit connection =>
      SQL("SELECT id, name " +
        " FROM genres " +
        " WHERE id = {id}").on('id -> genreid).single(Genres.parser)
    }
  }
  
  def delete(genreid: Long) {
    val genre = Genres.byId(genreid)
    Artists.byGenre(genreid).foreach(artist => Artists.delete(artist.id))
    new File(Release.genreLocation(genre)).delete
    DB.withConnection { implicit connection =>
      SQL("DELETE FROM genres " +
        " WHERE id = {id} ").on('id -> genreid).execute
    }
  }
}