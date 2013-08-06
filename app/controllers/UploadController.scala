package controllers

import play.api.mvc._
import java.io.File
import service._

object UploadController extends AbstractController {
  def uploadPage = withAuth { implicit request =>
    Ok(views.html.upload())
  }

  def upload = Action(parse.multipartFormData) { implicit request =>
    request.body.file("release").map { release =>
      val file = new File(Settings.repositoryLocation + release.filename)
      release.ref.moveTo(file)
      Log.debug("Uploaded " + release.filename)
      Ok("File uploaded")
    }.getOrElse {
      Redirect(routes.Application.index).flashing(
        "error" -> "Missing file")
    }
  }
}