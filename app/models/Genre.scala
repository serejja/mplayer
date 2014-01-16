package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._
import java.io.File
import service.Release
import play.api.data._
import play.api.data.Forms._

case class Genre(id: Long, name: String) {
  def toJson: JsValue = {
    Genres.writer.writes(this)
  }
}

object Genres {
  val columns = " g.id AS genre$id, g.name AS genre$name "
  private val from = " FROM genres g "    
    
  val parser = (
    get[Long]("genre$id") ~
    get[String]("genre$name") map {
      case id ~ name => Genre(id, name)
    })

  val optionParser = (
    get[Long]("genre$id") ~
    get[String]("genre$name") map {
      case id ~ name => (id.toString -> name)
    })

  implicit val writer = new Writes[Genre] {
    def writes(c: Genre): JsValue = {
      Json.obj("id" -> c.id,
        "name" -> c.name)
    }
  }
  
  val requestForm = Form(
    mapping("id" -> longNumber,
      "name" -> nonEmptyText)(Genre.apply)(Genre.unapply))

  def all: List[Genre] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT " + columns +
        from +
        " ORDER BY name").as(Genres.parser *)
    }
  }

  def byId(genreid: Long): Genre = {
    DB.withConnection { implicit connection =>
      SQL("SELECT " + columns +
        from +
        " WHERE id = {id}").on('id -> genreid).single(Genres.parser)
    }
  }

  def byAlbumId(albumid: Long): Genre = {
    DB.withConnection { implicit connection =>
      SQL("SELECT " + columns + 
        from +
        " INNER JOIN artists ar on g.id = ar.genre_id" +
        " INNER JOIN albums al on ar.id = al.artist_id" +
        " WHERE al.id = {id}" +
        " LIMIT 1").on('id -> albumid).single(Genres.parser)
    }
  }
  
  def update(genre: Genre) {
    DB.withConnection { implicit connection =>
      SQL("UPDATE genres " +
        " SET name = {name} " +
        " WHERE id = {id}").on('id -> genre.id, 'name -> genre.name).execute
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

  def comboOptions: Seq[(String, String)] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT id AS genre$id, name AS genre$name" +
        " FROM genres").as(optionParser *).sortBy(_._2)
    }
  }
}