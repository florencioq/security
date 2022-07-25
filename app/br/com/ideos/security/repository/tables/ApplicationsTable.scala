package br.com.ideos.security.repository.tables

import br.com.ideos.security.models.Application
import br.com.ideos.security.repository.SchemaName
import slick.jdbc.PostgresProfile.api._

class ApplicationsTable(tag: Tag) extends Table[Application](tag, SchemaName, "applications") {

  def appKey = column[String]("key", O.PrimaryKey)
  def name = column[String]("name")
  def webappUrl = column[Option[String]]("webapp_url")

  def * = (
    appKey,
    name,
    webappUrl,
  ) <> ((Application.apply _).tupled, Application.unapply)
}
