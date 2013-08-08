package models

import java.io.File
import service.FileUtils._

abstract class FormatHandler {
  def handleFile(file: File, destination: String, album: Album): Track
}

object Formats {
  private lazy val formatHandlers: Map[String, FormatHandler] = Map("mp3" -> Mp3Handler)

  def handle(file: File, destinationLocation: String, album: Album): Track = {
    formatHandlers.getOrElse(extension(file), new FormatHandler { def handleFile(file: File, destination: String, album: Album): Track = {null} }).handleFile(file, destinationLocation, album)
  }
}