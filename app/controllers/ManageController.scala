package controllers

import models.Genres
import models.Artists

object ManageController extends AbstractController {
	def genresPage = withAuth { implicit request =>
	  Ok(views.html.edit.genres(Genres.all))
	}
	
	def deleteGenre(id: Long) = withAuth { implicit request =>
	  Genres.delete(id)
	  Redirect(routes.ManageController.genresPage)
	}
	
	def artistsPage(genreid: Long) = withAuth { implicit request =>
	  Ok(views.html.edit.artists(Artists.byGenre(genreid)))
	}
}