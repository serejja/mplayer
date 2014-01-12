package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._

case class Country(id: Long, name: String) {
  def toJson: JsValue = {
    Countries.writer.writes(this)
  }
}

object Countries {
  val parser = (
    get[Long]("country$id") ~
    get[String]("country$name") map {
      case id ~ name => Country(id, name)
    })

  val optionParser = (
    get[Long]("country$id") ~
    get[String]("country$name") map {
      case id ~ name => (id.toString -> name)
    })

  def comboOptions: Seq[(String, String)] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT id AS country$id, name AS country$name" +
        " FROM countries").as(optionParser *)
    }
  }

  implicit val writer = new Writes[Country] {
    def writes(c: Country): JsValue = {
      Json.obj("id" -> c.id,
        "name" -> c.name)
    }
  }

  def withArtists: List[Country] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT DISTINCT c.id AS country$id, c.name AS country$name " +
        " FROM countries c " +
        " INNER JOIN artists a ON c.id = a.country_id " +
        " ORDER BY country$name").as(Countries.parser *)
    }
  }

  def byId(id: Long): Country = {
    null
  }
}