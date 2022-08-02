package br.com.ideos.security

import slick.jdbc.PostgresProfile.api._

package object repository {
  val SchemaName: Option[String] = Some("security")

  val db = Database.forConfig("db.default") // Not sure if it's the best place for this
}
