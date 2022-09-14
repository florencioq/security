package br.com.ideos.security.repository.tables

import br.com.ideos.security.model.AdminRef
import slick.jdbc.PostgresProfile.api._

class AdminsTable(tag: Tag) extends Table[AdminRef](tag, SchemaName, "admins") {
  val userId = column[Long]("user_id", O.PrimaryKey)
  def * = userId <> (AdminRef.apply, AdminRef.unapply)
}
