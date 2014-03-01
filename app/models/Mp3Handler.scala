package models

import java.io.File
import com.mpatric.mp3agic.Mp3File
import service.Log

object Mp3Handler extends FormatHandler {
  def handleFile(file: File, destination: String, album: Album): Track = {
    val mp3File = new Mp3File(file.getAbsolutePath)
    val name = getName(mp3File)
    val duration = Tracks.parseDuration(mp3File.getLengthInSeconds())
    val location = destination + file.getName
    val trackNo = getTrackNo(mp3File)
    val albumid = album.id
    if (!file.renameTo(new File(location))) {
      Log.warn("Path " + location + " already exists. Ignoring file")
      file.delete
    }

    Tracks.add(name, duration, location, trackNo, albumid)
  }

  private def getName(mp3File: Mp3File): String = {
    val v2 = mp3File.getId3v2Tag()
    if (v2 != null && v2.getTitle() != null) {
      v2.getTitle
    } else {
      mp3File.getId3v1Tag().getTitle
    }
  }
  
  private def getTrackNo(mp3File: Mp3File): Int = {
    val v2 = mp3File.getId3v2Tag()
    (if (v2 != null && v2.getTrack() != null) {
      v2.getTrack
    } else {
      mp3File.getId3v1Tag().getTrack
    }).toCharArray().filter(_.isDigit).mkString.toInt
  }
}