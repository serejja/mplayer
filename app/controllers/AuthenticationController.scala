package controllers

import controllers._
import play.api._
import play.api.mvc._
import models.User

object AuthenticationController extends Controller {
  def loginPage() = Action { implicit request =>
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
          Redirect(routes.Application.index).withSession("logged" -> "y", "username" -> value.name, "userid" -> value.id.toString)
        } else {
          Redirect(routes.AuthenticationController.loginPage)
        }
      })
  }

  def logout = Action { implicit request =>
    Ok(views.html.login(User.requestForm)).withNewSession
  }
}