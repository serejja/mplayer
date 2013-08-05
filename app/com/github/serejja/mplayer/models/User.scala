package com.github.serejja.mplayer.models

import play.api.data._
import play.api.data.Forms._
import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current

case class User(name: String, password: String)

object User {
  val requestForm = Form(
    mapping("name" -> nonEmptyText,
      "password" -> nonEmptyText)(User.apply)(User.unapply))

  def authenticate(user: User): Boolean = {
    DB.withConnection { implicit connection =>
      val count = SQL("SELECT COUNT(*) AS cnt FROM users WHERE LOWER(name) = LOWER({name}) AND password = md5({password})").on('name -> user.name, 'password -> user.password).single(get[Long]("cnt"))
      count > 0
    }
  }
}