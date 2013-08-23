package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current

case class Country(id: Long, name: String)

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
}