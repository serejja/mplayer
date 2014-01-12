package controllers

import play.api.libs.json.Json
import models.Countries

object CountryController extends AbstractController {
	def countries = withAuth { implicit request =>
	  import models.Countries._
	  Ok(Json.toJson(Countries.withArtists))
	}
}