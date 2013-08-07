package service

import java.io.File
import java.io.RandomAccessFile
import java.util.zip.ZipFile
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.io.BufferedOutputStream
import com.github.junrar.Archive
import com.github.junrar.impl.FileVolumeManager

object FileUtils {
  val zipBytes = Array[Byte]('P', 'K', 0x3, 0x4)
  val rarBytes = Array[Byte]('R', 'a', 'r', '!')

  def isZip(file: File): Boolean = {
    compareBytes(file, zipBytes)
  }

  def isRar(file: File): Boolean = {
    compareBytes(file, rarBytes)
  }

  private def compareBytes(file: File, bytes: Array[Byte]): Boolean = {
    val buffer = new Array[Byte](bytes.length)
    val randomAccessFile = new RandomAccessFile(file, "r")
    randomAccessFile.readFully(buffer)
    for (i <- 0 until bytes.length) {
      if (buffer(i) != bytes(i)) {
        return false
      }
    }
    true
  }

  def unzip(file: File) {
    val fileName = file.getAbsolutePath
    val zip = new ZipFile(file)
    val newPath = fileName.substring(0, fileName.length - 4)
    val bufferSize = 2048

    new File(newPath).mkdir
    val zipFileEntries = zip.entries
    while (zipFileEntries.hasMoreElements) {
      val entry = zipFileEntries.nextElement
      val currentEntry = entry.getName
      val destFile = new File(newPath, currentEntry)
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
          is.close
        }
      }
      if (currentEntry.endsWith(".zip"))
        unzip(destFile)
    }
    zip.close
  }

  def unrar(file: File) {
    val newPath = file.getParentFile.getAbsolutePath
    val archive = new Archive(new FileVolumeManager(file))
    try {
      var fileHeader = archive.nextFileHeader
      while (fileHeader != null) {
        if (!fileHeader.isDirectory) {
          val outFile = new File(newPath + "/" + fileHeader.getFileNameString)
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
}