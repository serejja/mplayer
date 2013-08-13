package controllers

import models.UserSettings
import play.api.data.Form

object UserController extends AbstractController {
  def settingsPage(id: Long) = withAuth { implicit request =>
    Ok(views.html.usersettings(UserSettings.requestForm.fill(UserSettings.byId(id))))
  }

  def saveSettings = withAuth { implicit request =>
    val settings = UserSettings.requestForm.bindFromRequest
    settings.fold(
      formWithErrors => {
        System.out.println(formWithErrors.errorsAsJson)
        Ok("Something went wrong, try again")
      },
      value => {
        UserSettings.save(value)
        Ok(views.html.session(value.id))
      })
  }
}