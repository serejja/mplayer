package service

import java.io.File
import java.io.RandomAccessFile
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.io.BufferedOutputStream
import com.github.junrar.Archive
import com.github.junrar.impl.FileVolumeManager
import models.Tracks
import java.nio.charset.Charset
import java.util.zip.ZipFile
import java.nio.file.Files
import java.nio.file.Paths

object FileUtils {
  val legalCharacters = List(32, 40, 41, 45, 46, 95) ++ (48 to 57) ++ (65 to 90) ++ (97 to 122)
  val zipBytes = Array[Byte]('P', 'K', 0x3, 0x4)
  val rarBytes = Array[Byte]('R', 'a', 'r', '!')
  val mp3Bytes = Array[Byte]('R', 'a', 'r', '!')

  def isZip(file: File): Boolean = {
    compareBytes(file, zipBytes) && file.getName.endsWith(".zip")
  }

  def isRar(file: File): Boolean = {
    compareBytes(file, rarBytes) && file.getName.endsWith(".rar")
  }

  def isMp3(file: File): Boolean = {
    file.getName.endsWith(".mp3")
  }

  private def compareBytes(file: File, bytes: Array[Byte]): Boolean = {
    val buffer = new Array[Byte](bytes.length)
    val randomAccessFile = new RandomAccessFile(file, "r")
    randomAccessFile.readFully(buffer)
    for (i <- 0 until bytes.length) {
      if (buffer(i) != bytes(i)) {
        randomAccessFile.close
        return false
      }
    }
    randomAccessFile.close
    true
  }

  def unzip(file: File) {
    var indexIfNeeded = 0
    val fileName = file.getAbsolutePath
    val zip = new ZipFile(file, Charset.forName("Cp1251"))
    val newPath = fileName.substring(0, fileName.length - 4)
    val bufferSize = 2048

    new File(newPath).mkdir
    val zipFileEntries = zip.entries
    while (zipFileEntries.hasMoreElements) {
      val entry = zipFileEntries.nextElement
      val currentEntry = replaceIllegalCharacters(entry.getName.substring(entry.getName.lastIndexOf('/') + 1)).replaceAll(" +", " ").trim
      val destFile = if (Files.exists(Paths.get(newPath + currentEntry))) {
        indexIfNeeded += 1
        new File(newPath, indexIfNeeded + "_" + currentEntry)
      } else {
        new File(newPath, currentEntry)
      }
      if (Tracks.formatSupported(destFile)) {
        val destinationParent = destFile.getParentFile
        destinationParent.mkdirs
        if (!entry.isDirectory) {
          val is = new BufferedInputStream(zip.getInputStream(entry))
          val data = new Array[Byte](bufferSize)
          val fos = new FileOutputStream(destFile)
          val dest = new BufferedOutputStream(fos, bufferSize)
          try {
            var currentByte = is.read(data, 0, bufferSize)
            while (currentByte != -1) {
              dest.write(data, 0, currentByte)
              currentByte = is.read(data, 0, bufferSize)
            }
          } finally {
            dest.flush
            dest.close
            fos.flush
            fos.close
            is.close
          }
        }
      }
    }
    zip.close
  }

  private def replaceIllegalCharacters(str: String): String = {
    str.filter(x => legalCharacters.contains(x.toInt))
  }

  def unrar(file: File) {
    val filePath = file.getAbsolutePath
    val newPath = filePath.substring(0, filePath.length - 4)
    val archive = new Archive(new FileVolumeManager(file))
    try {
      var fileHeader = archive.nextFileHeader
      while (fileHeader != null) {
        if (!fileHeader.isDirectory) {
          val outFile = new File(newPath + "/" + replaceIllegalCharacters(fileHeader.getFileNameString))
          val parent = outFile.getParentFile.mkdirs
          val os = new FileOutputStream(outFile)
          archive.extractFile(fileHeader, os)
          os.close
        }
        fileHeader = archive.nextFileHeader
      }
    } finally {
      archive.close
    }
  }

  def extension(file: File): String = {
    if (file != null) {
      val name = file.getName
      val index = name.lastIndexOf('.')
      if (index != -1) {
        return name.substring(index + 1)
      }
    }
    ""
  }
}