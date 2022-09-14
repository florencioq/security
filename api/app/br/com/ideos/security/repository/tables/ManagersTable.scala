package br.com.ideos.security.repository.tables

import br.com.ideos.security.model.Manager
import slick.jdbc.PostgresProfile.api._

class ManagersTable(tag: Tag) extends Table[Manager](tag, SchemaName, "managers") {
  val userId = column[Long]("user_id")
  val appKey = column[String]("app_key")

  def * = (userId, appKey) <> ((Manager.apply _).tupled, Manager.unapply)
}
