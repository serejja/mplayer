package controllers

import play.api.mvc._
import java.io.File
import service._
import scala.collection.mutable.HashMap

object UploadController extends AbstractController {
  private val PARAM_GENRE = "genre"
  private val PARAM_ARTIST = "artist"
  private val PARAM_ALBUM = "album"
  private val PARAM_YEAR = "year"

  def uploadPage = withAuth { implicit request =>
    Ok(views.html.upload())
  }

  def upload = Action(parse.multipartFormData) { implicit request =>
    request.body.file("release").map { release =>
      val formParameters = requestParameters(request.body.asFormUrlEncoded)
      if (isValid(formParameters)) {
        val file = new File(Settings.repositoryLocation + release.filename)
        release.ref.moveTo(file)
        Release.fromFile(file, formParameters)
        Log.debug("Uploaded " + release.filename)
        Ok("File uploaded")
      } else {
        Log.warn("Not all parameters supplied for release " + release.filename)
        Ok("Not all parameters supplied")
      }
    }.getOrElse {
      Redirect(routes.Application.index).flashing(
        "error" -> "Missing file")
    }
  }

  private def requestParameters(form: Map[String, Seq[String]]): HashMap[String, String] = {
    val result = HashMap[String, String]()
    result += "artist" -> form.getOrElse("artist", Seq("noartist")).head
    result += "album" -> form.getOrElse("album", Seq("noalbum")).head
    result += "year" -> form.getOrElse("year", Seq("noyear")).head

    result
  }

  private def isValid(params: HashMap[String, String]): Boolean = {
    return params.getOrElse(PARAM_ARTIST, "noartist") != "noartist" && params.getOrElse(PARAM_ALBUM, "noalbum") != "noalbum" && params.getOrElse(PARAM_YEAR, "noyear") != "noyear"
  }
}