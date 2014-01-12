package controllers

import play.api._
import play.api.mvc._
import scala.io.Source
import java.io.File
import play.api.data.Form
import models.User
import play.api.libs.json.Json
import models._
import java.io.FileInputStream
import play.api.libs.iteratee.Enumerator

object Application extends AbstractController {
  def index = withAuth { implicit request =>
    Ok(views.html.index(request))
  }

  def genres = withAuth { implicit request =>
    import models.Genres._
    Ok(Json.toJson(Genres.all))
  }

  def albums(artistid: Long) = withAuth { implicit request =>
    import models.Albums._
    Ok(Json.toJson(Albums.byArtist(artistid)))
  }

  def tracks(albumid: Long, artistid: Long, genreid: Long, countryid: Long) = withAuth { implicit request =>
    import models.Tracks._
    if (albumid != -1) {
      Ok(Json.toJson(Tracks.byAlbum(albumid)))
    } else if (artistid != -1) {
      Ok(Json.toJson(Tracks.byArtist(artistid)))
    } else if (genreid != -1) {
      Ok(Json.toJson(Tracks.byGenre(genreid)))
    } else {
      Ok(Json.toJson(Tracks.byCountry(countryid)))
    }
  }

  def trackinfo(id: Long) = withAuth { implicit request =>
    import models.Tracks._
    Ok(Json.toJson(Tracks.byId(id)))
  }

  def get(id: Long) = withAuth { implicit request =>
    val track = Tracks.byId(id)
    val file = new File(track.location)

    request.headers.get("Range") match {
      case Some(value) => {
        val range: (Long, Long) = value.substring("bytes=".length).split("-") match {
          case x if x.length == 1 => (x.head.toLong, file.length() - 1)
          case x => (x(0).toLong, x(1).toLong)
        }

        range match {
          case (start, end) =>
            val stream = new FileInputStream(file)
            stream.skip(start)
            SimpleResult(
              header = ResponseHeader(PARTIAL_CONTENT,
                Map(
                  CONNECTION -> "keep-alive",
                  ACCEPT_RANGES -> "bytes",
                  CONTENT_RANGE -> "bytes %d-%d/%d".format(start, end, file.length()),
                  CONTENT_LENGTH -> (end - start + 1).toString,
                  CONTENT_TYPE -> play.api.libs.MimeTypes.forExtension("mp3").get)),
              body = Enumerator.fromStream(stream))
        }
      }
      case None => Ok.sendFile(file).as("audio/mpeg").withHeaders("Accept-Ranges" -> "bytes")
    }
  }

  def test = Action {
    Ok(views.html.test())
  }
}