package br.com.ideos.security.repository.tables

import br.com.ideos.security.model.Permission
import br.com.ideos.security.repository.SchemaName
import slick.jdbc.PostgresProfile.api._

class PermissionsTable(tag: Tag) extends Table[Permission](tag, SchemaName, "permissions") {

  def userId = column[Long]("user_id")
  def roleId = column[Long]("role_id")

  def * = (
    userId,
    roleId,
  ) <> ((Permission.apply _).tupled, Permission.unapply)
}
