package br.com.ideos.security.repository.tables

import br.com.ideos.security.model.Role
import slick.jdbc.PostgresProfile.api._

class RolesTable(tag: Tag) extends Table[Role](tag, SchemaName, "roles") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def appKey = column[String]("app_key")

  def * = (
    id,
    name,
    appKey,
  ) <> ((Role.apply _).tupled, Role.unapply)
}
