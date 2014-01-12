package models

import play.api.data._
import play.api.data.Forms._
import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current

case class User(name: String, password: String) {
  lazy val id = DB.withConnection { implicit connection =>
    SQL("SELECT id FROM users WHERE LOWER(name) = LOWER({name})").on('name -> this.name).single(get[Long]("id"))
  }

  lazy val sessionKey = DB.withConnection { implicit connection =>
    SQL("SELECT lastfm_session_key FROM users WHERE id = {id}").on('id -> this.id).single(get[String]("lastfm_session_key"))
  }
}

object User {
  val parser = (
    get[String]("name") ~
    get[String]("password") map {
      case name ~ pwd => User(name, pwd)
    })

  val requestForm = Form(
    mapping("name" -> nonEmptyText,
      "password" -> nonEmptyText)(User.apply)(User.unapply))

  def authenticate(user: User): Boolean = {
    DB.withConnection { implicit connection =>
      val count = SQL("SELECT COUNT(*) AS cnt FROM users WHERE LOWER(name) = LOWER({name}) AND password = md5({password})").on('name -> user.name, 'password -> user.password).single(get[Long]("cnt"))
      count > 0
    }
  }

  def byId(id: Long): User = {
    DB.withConnection { implicit connection =>
      SQL("SELECT name, password FROM users WHERE id = {id}").on('id -> id).single(User.parser)
    }
  }

  def isValidInvitation(invitation: String): Boolean = {
    val count = DB.withConnection { implicit connection =>
      SQL("SELECT count(*) AS CNT FROM invites WHERE invite = {invite} AND used = '0'").on('invite -> invitation).single(get[Long]("cnt"))
    }
    count > 0
  }
}