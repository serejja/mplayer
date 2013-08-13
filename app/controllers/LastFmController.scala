package controllers

import play.api.libs.ws.WS
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs._
import play.api.libs.concurrent._
import play.api.libs.json._
import service.Settings
import service.Token
import service.LastFm
import models.UserSettings
import models.User

object LastFmController extends AbstractController {
  def token = withAuth { implicit request =>
    Ok(Token.actual.token)
  }

  def authorize(id: Long) = withAuth { implicit request =>
    Redirect(s"http://www.last.fm/api/auth/?api_key=${Settings.lastfmApiKey}&token=${LastFm.requestToken.token}")
  }

  def getSessionKey(id: Long) = withAuth { implicit request =>
    val sessionKey = LastFm.requestSession
    UserSettings.updateSessionKey(id, sessionKey)
    Ok(views.html.index(request))
  }

  def updateNowPlaying(artist: String, track: String, userid: Long) = withAuth { implicit request =>
    LastFm.updateNowPlaying(artist, track, User.byId(userid))
    Ok("")
  }
}