package controllers

import play.api.libs.json.Json
import models.Genres

object GenreController extends AbstractController {
  def genres = withAuth { implicit request =>
    import models.Genres._
    Ok(Json.toJson(Genres.all))
  }

  def genre(id: Long) = withAuth { implicit request =>
    Ok(Genres.byId(id).toJson)
  }

  def update = withAuth { implicit request =>
    Genres.requestForm.bindFromRequest.fold(
      errors => {
        Ok(errors.errorsAsJson)
      },
      genre => {
        Genres.update(genre)
        Ok("Save genre OK")
      })
  }
}