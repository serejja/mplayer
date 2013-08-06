package controllers

import play.api.mvc._

abstract class AbstractController extends Controller {
  def withAuth(authenticated: Request[AnyContent] => Result) = Action { implicit request =>
    val logged = request.session.get("logged").getOrElse("n")
    if (logged != "y") {
      Redirect(routes.AuthenticationController.loginPage)
    } else {
      authenticated(request)
    }
  }
}