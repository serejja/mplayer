package models

import play.api.data._
import play.api.data.Forms._
import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current

case class UserSettings(id: Long, lastfmAccount: Option[String], sessionKey: Option[String])

object UserSettings {
  val requestForm = Form(
    mapping("id" -> longNumber,
      "account" -> optional(text),
      "sessionKey" -> optional(text))(UserSettings.apply)(UserSettings.unapply))

  lazy val parser = (
    get[Long]("id") ~
    get[Option[String]]("lastfm_account") ~
    get[Option[String]]("lastfm_session_key") map {
      case id ~ account ~ session => UserSettings(id, account, session)
    })

  def byId(id: Long): UserSettings = {
    val list = DB.withConnection { implicit connection =>
      SQL("SELECT id, lastfm_account, lastfm_session_key " +
        " FROM users " +
        " WHERE id = {id}").on('id -> id).as(UserSettings.parser *)
    }
    if (list.length == 0) null else list.head
  }

  def save(settings: UserSettings) {
    DB.withConnection { implicit connection =>
      SQL("UPDATE users " +
        " SET lastfm_account = {account}, lastfm_session_key = {session} " +
        " WHERE id = {id}").on('id -> settings.id, 'account -> settings.lastfmAccount, 'session -> settings.sessionKey).execute
    }
  }

  def updateSessionKey(id: Long, key: String) {
    DB.withConnection { implicit connection =>
      SQL("UPDATE users " +
        " SET lastfm_session_key = {session} " +
        " WHERE id = {id}").on('id -> id, 'session -> key).execute
    }
  }
}