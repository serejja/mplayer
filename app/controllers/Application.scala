package controllers

import play.api._
import play.api.mvc._
import scala.io.Source
import java.io.File
import play.api.data.Form
import com.github.serejja.mplayer.models.User

object Application extends Controller {

  def index = Action { implicit request =>
    val logged = request.session.get("logged").getOrElse("n")
    if (logged != "y") {
      Redirect(routes.Application.loginPage)
    } else {
      Ok(views.html.index("Your new application is ready."))
    }
  }

  def get = Action {
    Ok.sendFile(content = new File("c:/darkness.mp3"))
  }

  def loginPage() = Action {
    Ok(views.html.login(User.requestForm))
  }

  def login = Action { implicit request =>
    val user = User.requestForm.bindFromRequest
    user.fold(
      formWithErrors => {
        Redirect(routes.Application.loginPage)
      },
      value => {
        if (User.authenticate(value)) {
          Ok(views.html.index("hello")).withSession("logged" -> "y")
        } else {
          Redirect(routes.Application.loginPage)
        }
      })
  }
}