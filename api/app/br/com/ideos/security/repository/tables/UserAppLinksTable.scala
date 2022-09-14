package br.com.ideos.security.repository.tables

import br.com.ideos.security.model.user.UserAppLink
import slick.jdbc.PostgresProfile.api._

import java.time.Instant

class UserAppLinksTable(tag: Tag) extends Table[UserAppLink](tag, SchemaName, "user_app_links") {

  def userId = column[Long]("user_id")
  def appKey = column[String]("app_key")

  def disabled = column[Boolean]("disabled")

  def createdAt = column[Instant]("created_at")

  def * = (
    userId,
    appKey,
    disabled,
    createdAt,
  ) <> ((UserAppLink.apply _).tupled, UserAppLink.unapply)
}
