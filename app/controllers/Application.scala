package controllers

import play.api._
import play.api.mvc._
import scala.io.Source
import java.io.File
import play.api.data.Form
import models.User
import play.api.libs.json.Json
import models.Genres
import models.Genres._

object Application extends Controller {
  def index = withAuth { implicit request =>
    Ok(views.html.index(request))
  }
  
  def genres = withAuth { implicit request =>
    Ok(Json.toJson(Genres.all)).as("application/json")
  }

  def get(id: Long) = withAuth { implicit request =>
    if (id == 1) {
      Ok.sendFile(content = new File("c:/darkness.mp3"))
    } else {
      Ok.sendFile(content = new File("c:/monody.mp3"))
    }
  }

  def withAuth(authenticated: Request[AnyContent] => Result) = Action { implicit request =>
    val logged = request.session.get("logged").getOrElse("n")
    if (logged != "y") {
      Redirect(routes.AuthenticationController.loginPage)
    } else {
      authenticated(request)
    }
  }
}