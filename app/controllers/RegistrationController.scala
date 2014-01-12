package controllers

import models.User
import models.Registration
import play.api.libs.json.Json._

object RegistrationController extends AbstractController {
  def register(invitation: String) = withAuth { implicit request =>
    Registration.registerForm.bindFromRequest.fold(
      formWithErrors => {
        val msg = stringify(formWithErrors.errorsAsJson).replace("{", "").replace("}", "").replaceAll("[\\[\\]\"]", "")
        Ok(views.html.registration.register(invitation, msg))
      },
      value => {
        Registration.register(value)
        Registration.invalidateInvite(invitation)
        Redirect(routes.AuthenticationController.loginPage)
      })
  }

  def registerPage(invitation: String) = withAuth { implicit request =>
    if (User.isValidInvitation(invitation)) {
      Ok(views.html.registration.register(invitation))
    } else {
      Redirect(routes.AuthenticationController.loginPage)
    }
  }
}