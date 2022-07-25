package br.com.ideos.security.models.token

import play.api.libs.json.{Format, Json}

import java.util.UUID

case class PasswordRedefinitionTokenPayload(
  userId: Long,
  redefinitionId: UUID,
  override val kind: String = TokenType.PasswordRedefinition,
) extends TokenPayload(kind)

object PasswordRedefinitionTokenPayload {
  implicit val format: Format[PasswordRedefinitionTokenPayload] = Json.format
}
