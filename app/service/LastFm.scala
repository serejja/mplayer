package service

import play.api.libs.ws.WS
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs._
import play.api.libs.concurrent._
import play.api.libs.json._
import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import java.util.Date
import java.sql.Timestamp
import scala.concurrent.Await
import scala.concurrent.duration._
import java.security.MessageDigest
import java.math.BigInteger
import java.lang.StringBuilder
import models.User
import java.net.URLEncoder

case class Token(id: Long, token: String, createdate: Date) {
  private val oneHour = 1000 * 60 * 60
  def expired: Boolean = {
    return createdate.getTime + oneHour <= System.currentTimeMillis
  }
}

object Token {
  implicit def rowToTimestamp: Column[Date] = {
    Column[Date](transformer = { (value, meta) =>
      val MetaDataItem(qualified, nullable, clazz) = meta
      value match {
        case time: Timestamp => Right(time)
        case _ => Left(TypeDoesNotMatch("Cannot convert " + value + " to Timestamp for column " + qualified))
      }
    })
  }

  val parser = (
    get[Long]("id") ~
    get[String]("token") ~
    get[Date]("create_date") map {
      case id ~ token ~ createdate => Token(id, token, createdate)
    })

  def create(token: String): Token = {
    DB.withConnection { implicit connection =>
      SQL("INSERT INTO last_fm_tokens (token) " +
        " VALUES ({token}) " +
        " RETURNING id, token, create_date").on('token -> token).single(Token.parser)
    }
  }

  def actual: Token = {
    val tokens = DB.withConnection { implicit connection =>
      SQL("SELECT id, token, create_date " +
        " FROM last_fm_tokens " +
        " ORDER BY create_date DESC " +
        " LIMIT 1").as(Token.parser *)
    }
    if (tokens.size == 0) {
      LastFm.requestToken
    } else {
      val lastToken = tokens.head
      System.out.println(lastToken.expired)
      if (lastToken.expired) LastFm.requestToken else lastToken
    }
  }
}

object LastFm {
  def requestToken: Token = {
    val futureToken = WS.url(s"http://ws.audioscrobbler.com/2.0/?method=auth.gettoken&api_key=${Settings.lastfmApiKey}&format=json").get.map { response =>
      (response.json \ "token").as[String]
    }
    val tokenKey = Await.result(futureToken, 5 seconds)
    Token.create(tokenKey)
  }

  def requestSession: String = {
    val token = Token.actual
    val futureSession = WS.url(s"http://ws.audioscrobbler.com/2.0/?method=auth.getSession&token=${token.token}&api_key=${Settings.lastfmApiKey}&api_sig=${md5(s"api_key${Settings.lastfmApiKey}methodauth.getSessiontoken${token.token}${Settings.lastfmSecret}")}&format=json").get.map { response =>
      System.out.println(response.json)
      (response.json \ "session" \ "key").as[String]
    }
    Await.result(futureSession, 5 seconds)
  }

  def updateNowPlaying(artist: String, track: String, user: User) {
    val method = "track.updateNowPlaying"
    val sessionKey = user.sessionKey
    val url = Settings.lastfmApiUrl
    val futureUpdate = WS.url(url).post(Map("method" -> Seq(method),
      "artist" -> Seq(artist),
      "track" -> Seq(track),
      "api_key" -> Seq(Settings.lastfmApiKey),
      "api_sig" -> Seq(md5(s"api_key${Settings.lastfmApiKey}artist${artist}method${method}sk${sessionKey}track${track}${Settings.lastfmSecret}")),
      "sk" -> Seq(sessionKey),
      "format" -> Seq("json"))).map { response =>
      System.out.println(response.json)
    }
    Await.result(futureUpdate, 5 seconds)
  }

  def scrobble(artist: String, track: String, timestamp: String, user: User) {
    val method = "track.scrobble"
    val sessionKey = user.sessionKey
    val url = Settings.lastfmApiUrl
    val futureScrobble = WS.url(url).post(Map("method" -> Seq(method),
      "artist" -> Seq(artist),
      "track" -> Seq(track),
      "timestamp" -> Seq(timestamp),
      "api_key" -> Seq(Settings.lastfmApiKey),
      "sk" -> Seq(sessionKey),
      "format" -> Seq("json"),
      "api_sig" -> Seq(md5(s"api_key${Settings.lastfmApiKey}artist${artist}method${method}sk${sessionKey}timestamp${timestamp}track${track}${Settings.lastfmSecret}")))).map { response =>
      System.out.println(response.json)
    }
    Await.result(futureScrobble, 5 seconds)
  }

  private def md5(s: String): String = {
    val digest = MessageDigest.getInstance("MD5")
    val bytes = digest.digest(s.getBytes("UTF-8"))
    val builder = new StringBuilder(32)
    for (aByte: Byte <- bytes) {
      val hex = Integer.toHexString(aByte & 0xFF)
      if (hex.length == 1) {
        builder.append('0')
      }
      builder.append(hex)
    }
    builder.toString
  }
}