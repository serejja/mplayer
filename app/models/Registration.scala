package models

import play.api.data._
import play.api.data.Forms._
import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current

case class RegisterUser(invite: String, name: String, password: String, confirmpassword: String, lastfm: Option[String], email: String)

object Registration {
  val registerForm = Form(
    mapping("invitation" -> nonEmptyText,
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "confirmpassword" -> nonEmptyText,
      "lastfm" -> optional(text),
      "email" -> nonEmptyText)(RegisterUser.apply)(RegisterUser.unapply)
      verifying ("User already exists", userNotExists(_))
      verifying ("Passwords do not match", passwordsMatch(_)))

  private def userNotExists(user: RegisterUser): Boolean = {
    val count = DB.withConnection { implicit connection =>
      SQL("SELECT count(*) AS cnt FROM users WHERE lower({name}) = name").on('name -> user.name).single(get[Long]("cnt"))
    }

    count == 0
  }

  private def passwordsMatch(user: RegisterUser): Boolean = {
    user.password == user.confirmpassword
  }

  def register(user: RegisterUser) {
    DB.withConnection { implicit connection =>
      SQL("INSERT INTO users (name, password, email, lastfm_account) VALUES ({name}, md5({password}), {email}, {lastfm})")
        .on('name -> user.name, 'password -> user.password, 'email -> user.email, 'lastfm -> user.lastfm.getOrElse(null)).execute
    }
  }
  
  def invalidateInvite(invite: String) {
    DB.withConnection { implicit connection =>
      SQL("UPDATE invites SET used = '1' WHERE invite = {invite}")
        .on('invite -> invite).execute
    }
  }
}