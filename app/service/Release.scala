package service

import java.io.File
import service.FileUtils._
import scala.collection.mutable.HashMap
import models.Genres
import models.Artists
import models.Albums
import models.Tracks
import models.Formats
import models.Album
import models.Genre
import models.Artist
import models.Album

object Release {
	def fromFile(file: File, genre: String, artistid: Option[String], artistname: Option[String], countryid: Option[String], album: String, year: String, format: String) {
	  if (isZip(file)) {
	    unzip(file)
	  } else if (isRar(file)) {
	    unrar(file)
	  } else {
	    Log.error("File " + file.getName + " is not a rar or zip archive")
	  }
	  val releaseTempLocation = file.getAbsolutePath.substring(0, file.getAbsolutePath.length - 4)
	  processFolder(releaseTempLocation, genre, artistid, artistname, countryid, album, year, format)
	}
	
	def processFolder(releaseTempLocation: String, genreid: String, artistid: Option[String], artistname: Option[String], countryid: Option[String], albumName: String, year: String, format: String) {
	  val artist = countryid match {
	    case Some(id) => Artists.create(genreid.toLong, artistname.get, id.toLong) 
	    case None => Artists.byId(artistid.get.toLong)
	  }
	  val destinationReleaseLocation = createReleaseFolders(artist.genre.id, artist.name, albumName, year)
	  val album = Albums.getOrNew(artist, albumName, year.toLong, format)
	  val folder = new File(releaseTempLocation)
	  recursiveAddFiles(folder.listFiles.sorted, destinationReleaseLocation, album)
	  folder.delete
	}
	
	def createReleaseFolders(genre: Long, artist: String, album: String, year: String): String = {
	  val releasePath = s"${Settings.repositoryLocation}${Genres.byId(genre).name}/${cleanString(artist)}/${year} - ${cleanString(album)}/"
	  new File(releasePath).mkdirs
	  releasePath
	}
	
	def recursiveAddFiles(files: Array[File], destinationLocation: String, album: Album) {
	  if (files != null) {
	    files.foreach(file => {
	      if (file.isDirectory) {
	        recursiveAddFiles(file.listFiles, destinationLocation, album)
	        file.delete
	      } else if (Tracks.formatSupported(file)) {
	        Log.debug("Handling " + file.getName)
	        Formats.handle(file, destinationLocation, album)
	      } else {
	        Log.debug(file.getName + " is not a supported media file, deleted")
	        file.delete
	      }
	    })
	  }
	}
	
	def albumLocation(genre: Genre, artist: Artist, album: Album): String = {
	  s"${artistLocation(genre, artist)}${album.year} - ${album.name}/"
	}
	
	def artistLocation(genre: Genre, artist: Artist): String = {
	  s"${genreLocation(genre)}${artist.name}/"
	}
	
	def genreLocation(genre: Genre): String = {
	  s"${Settings.repositoryLocation}${genre.name}/"
	}
	
	private def cleanString(toClean: String): String = toClean.replaceAll(Settings.specialCharacters, "")
}