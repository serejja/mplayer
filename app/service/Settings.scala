package service

import play.api.Play.current
import scala.collection.mutable.HashMap
import play.api.db.DB
import anorm._
import anorm.SqlParser._

case class Settings(name: String, value: String)

object Settings {
  private var isFilling = false

  private val REPO_LOCATION = "repo_location"
  private val TEMP_LOCATION = "temp_location"
  private val LAST_FM_API_KEY = "lastfm_api_key"
  private val LAST_FM_SECRET_KEY = "lastfm_secret"
  private val LAST_FM_API_URL = "lastfm_api_url"

  private lazy val parser = {
    get[String]("name") ~
      get[String]("value") map {
        case name ~ value => cache += name -> value
      }
  }

  private var cache = HashMap[String, String]()
  private var timestamp = 0L

  private def fillCache() = {
    if (!isFilling) {
      this.synchronized {
        if (!isFilling) {
          isFilling = true
          DB.withConnection {
            implicit connection =>
              SQL("SELECT name, value " +
                " FROM settings ").as(Settings.parser *)
          }
          timestamp = System.currentTimeMillis()
          isFilling = false
        }
      }

    }
  }

  private def getByName(name: String, defValue: String): String = {
    if ((System.currentTimeMillis - timestamp) > getSettingsCacheValidTime) {
      fillCache
    }
    cache.getOrElse(name, defValue)
  }

  private def getLongByName(name: String, defValue: Long): Long = {
    try {
      getByName(name, defValue.toString).toLong
    } catch {
      case e: Throwable => {
        defValue
      }
    }
  }

  def getSettingsCacheValidTime: Long = {
    300000L
  }

  def repositoryLocation: String = {
    getByName(REPO_LOCATION, "c:/music/")
  }

  def temporaryLocation: String = {
    repositoryLocation + getByName(TEMP_LOCATION, "temp/")
  }
  
  def lastfmApiKey: String = {
    getByName(LAST_FM_API_KEY, "3df5180904a9a83cd62ab535f0567e67")
  }
  
  def lastfmSecret: String = {
    getByName(LAST_FM_SECRET_KEY, "f35a156fccfe2963b6051e23c1c2aa99")
  }
  
  def lastfmApiUrl: String = { 
    getByName(LAST_FM_API_URL, "http://ws.audioscrobbler.com/2.0/")
  }
}