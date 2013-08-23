package controllers

import models.Artists
import play.api.libs.json.Json

object ArtistController extends AbstractController {
  def artist(id: Long) = withAuth { implicit request =>
    Ok(Artists.byId(id).toJson)
  }

  def artists(genreid: Long) = withAuth { implicit request =>
    import models.Artists._
    Ok(Json.toJson(Artists.byGenre(genreid)))
  }
}