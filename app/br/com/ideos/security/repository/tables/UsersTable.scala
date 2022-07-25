package br.com.ideos.security.repository.tables

import br.com.ideos.security.models.User
import br.com.ideos.security.repository.SchemaName
import slick.jdbc.PostgresProfile.api._

import java.time.Instant

class UsersTable(tag: Tag) extends Table[User](tag, SchemaName, "users") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def email = column[String]("email")
  def password = column[String]("password")

  def createdAt = column[Instant]("created_at")

  def * = (
    id,
    email,
    password,
    createdAt,
  ) <> ((User.apply _).tupled, User.unapply)
}
