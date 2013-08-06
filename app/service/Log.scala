package service

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current

object Log {
  private val DEBUG = "D"
  private val WARN = "W"
  private val ERROR = "E"
    
  def debug(text: String) {
    write(text, DEBUG)
  }
  
  def warn(text: String) {
    write(text, WARN)
  }
  
  def error(text: String) {
    write(text, ERROR)
  }
  
  private def write(text: String, level: String) {
    DB.withConnection { implicit connection =>
      SQL("INSERT INTO logs (log_text, log_level) VALUES ({text}, {level})").on('text -> text, 'level -> level).execute
    }
  }
}