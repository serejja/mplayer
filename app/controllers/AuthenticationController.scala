package controllers

import controllers._
import play.api._
import play.api.mvc._
import models.User

object AuthenticationController extends Controller {
	def loginPage() = Action {
    Ok(views.html.login(User.requestForm))
  }

  def login = Action { implicit request =>
    val user = User.requestForm.bindFromRequest
    user.fold(
      formWithErrors => {
        Redirect(routes.AuthenticationController.loginPage)
      },
      value => {
        if (User.authenticate(value)) {
          Ok(views.html.index("hello")).withSession("logged" -> "y")
        } else {
          Redirect(routes.AuthenticationController.loginPage)
        }
      })
  }
}