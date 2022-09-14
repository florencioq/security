package br.com.ideos.security.repository.tables

import br.com.ideos.security.model.PasswordRedefinitionAttempt
import slick.jdbc.PostgresProfile.api._

import java.util.UUID

class PasswordRedefinitionTable(tag: Tag) extends Table[PasswordRedefinitionAttempt](tag, SchemaName, "password_redefinitions") {

  def userId = column[Long]("user_id")
  def redefinitionId = column[UUID]("redefinition_uuid")

  def * = (
    userId,
    redefinitionId,
  ) <> ((PasswordRedefinitionAttempt.apply _).tupled, PasswordRedefinitionAttempt.unapply)
}
