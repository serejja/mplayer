package controllers

import models.Artists
import play.api.libs.json.Json

object ArtistController extends AbstractController {
  def artist(id: Long) = withAuth { implicit request =>
    Ok(Artists.byId(id).toJson)
  }

  def artists(genreid: Long, countryid: Long) = withAuth { implicit request =>
    import models.Artists._
    if (genreid != -1) {
      Ok(Json.toJson(Artists.byGenre(genreid)))
    } else {
      Ok(Json.toJson(Artists.byCountry(countryid)))
    }
  }

  def update = withAuth { implicit request =>
    Artists.requestForm.bindFromRequest.fold(
      errors => {
        Ok(errors.errorsAsJson)
      },
      artist => {
        Artists.update(artist)
        Ok("Save artist OK")
      })
  }
}