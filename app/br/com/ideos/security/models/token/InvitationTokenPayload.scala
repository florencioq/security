package br.com.ideos.security.models.token

import play.api.libs.json.{Format, Json}

case class InvitationTokenPayload(
  email: String,
  appKey: String,
  override val kind: String = TokenType.FirstAccess,
) extends TokenPayload(kind)

object InvitationTokenPayload {
  implicit def format: Format[InvitationTokenPayload] = Json.format
}