package models

import java.io.File
import com.mpatric.mp3agic.Mp3File
import service.Log

object Mp3Handler extends FormatHandler {
	def handleFile(file: File, destination: String, album: Album): Track = {
	  val mp3File = new Mp3File(file.getAbsolutePath)
	  val name = mp3File.getId3v2Tag().getTitle
	  val duration = Tracks.parseDuration(mp3File.getLengthInSeconds())
	  val location = destination + file.getName
	  val trackNo = mp3File.getId3v2Tag().getTrack().toCharArray().filter(_.isDigit).mkString.toInt
	  val albumid = album.id
	  if (!file.renameTo(new File(location))) {
	    Log.warn("Path " + location + " already exists. Ignoring file")
	    file.delete
	  }
	  
	  Tracks.add(name, duration, location, trackNo, albumid)
	}
}