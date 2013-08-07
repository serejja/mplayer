package service

import java.io.File
import service.FileUtils._
import scala.collection.mutable.HashMap

object Release {
	def fromFile(file: File, params: HashMap[String, String]) {
	  if (isZip(file)) {
	    unzip(file)
	    file.delete
	  } else if (isRar(file)) {
	    unrar(file)
	    file.delete
	  } else {
	    Log.error("File " + file.getName + " is not a rar or zip archive")
	  }
	}
	
	
}