package controllers

import play.api._
import play.api.mvc._
import scala.io.Source
import java.io.File
import play.api.data.Form
import models.User
import play.api.libs.json.Json
import models._

object Application extends AbstractController {
  def index = withAuth { implicit request =>
    Ok(views.html.index(request))
  }
  
  def genres = withAuth { implicit request =>
    import models.Genres._
    Ok(Json.toJson(Genres.all))
  }
  
  def artists(genreid: Long) = withAuth { implicit request =>
    import models.Artists._
    Ok(Json.toJson(Artists.byGenre(genreid)))
  }
  
  def albums(artistid: Long) = withAuth { implicit request =>
    import models.Albums._
    Ok(Json.toJson(Albums.byArtist(artistid)))
  }
  
  def tracks(albumid: Long, artistid: Long, genreid: Long) = withAuth { implicit request =>
    import models.Tracks._
    if (albumid != -1) {
    	Ok(Json.toJson(Tracks.byAlbum(albumid)))
    } else if (artistid != -1) {
      Ok(Json.toJson(Tracks.byArtist(artistid)))
    } else {
      Ok(Json.toJson(Tracks.byGenre(genreid)))
    }
  }
  
  def trackinfo(id: Long) = withAuth { implicit request =>
    import models.Tracks._
    Ok(Json.toJson(Tracks.trackInfo(id)))
  }

  def get(id: Long) = withAuth { implicit request =>
    val track = Tracks.byId(id)
    Ok.sendFile(content = new File(track.location))
  }
}