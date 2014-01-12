package controllers

import play.api.mvc._
import java.io.File
import service._
import scala.collection.mutable.HashMap
import models.Genres
import models.Albums

object UploadController extends AbstractController {
  private val PARAM_GENRE = "genre"
  private val PARAM_ARTIST = "artist"
  private val PARAM_ALBUM = "album"
  private val PARAM_YEAR = "year"
  private val PARAM_FORMAT = "format"

  def uploadPage = withAuth { implicit request =>
    Ok(views.html.upload(Genres.comboOptions, Albums.getFormats))
  }

  def upload = Action(parse.multipartFormData) { implicit request =>
    request.body.file("release").map { release =>
      val formParameters = request.body.asFormUrlEncoded
      def getParam(name: String): String = {
        formParameters.getOrElse(name, Seq("")).head
      }
      if (isValid(request.body.asFormUrlEncoded)) {
        val file = new File(Settings.temporaryLocation + release.filename)
        release.ref.moveTo(file)
        Release.fromFile(file, getParam(PARAM_GENRE), getParam(PARAM_ARTIST), getParam(PARAM_ALBUM), getParam(PARAM_YEAR), getParam(PARAM_FORMAT))
        Log.debug("Uploaded " + release.filename)
        release.ref.clean
        file.delete()
        Ok("File uploaded")
      } else {
        Log.warn("Not all parameters supplied for release " + release.filename)
        Ok("Not all parameters supplied")
      }
    }.getOrElse {
      Ok("No file supplied")
    }
  }

  private def requestParameters(form: Map[String, Seq[String]]): HashMap[String, String] = {
    val result = HashMap[String, String]()
    result += "artist" -> form.getOrElse("artist", Seq("noartist")).head
    result += "album" -> form.getOrElse("album", Seq("noalbum")).head
    result += "year" -> form.getOrElse("year", Seq("noyear")).head

    result
  }

  private def isValid(params: Map[String, Seq[String]]): Boolean = {
    params.getOrElse(PARAM_GENRE, Seq("")).head != "" && params.getOrElse(PARAM_ARTIST, Seq("")).head != "" && params.getOrElse(PARAM_ALBUM, Seq("")).head != "" && params.getOrElse(PARAM_YEAR, Seq("")).head != "" && params.getOrElse(PARAM_FORMAT, Seq("")).head != ""
  }
}