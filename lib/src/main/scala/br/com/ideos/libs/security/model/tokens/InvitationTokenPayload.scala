package br.com.ideos.libs.security.model.tokens

import play.api.libs.json.{Format, Json}

case class InvitationTokenPayload(
  email: String,
  appKey: String,
  newUser: Boolean,
  override val kind: String = TokenType.FirstAccess,
) extends TokenPayload(kind)

object InvitationTokenPayload {
  implicit def format: Format[InvitationTokenPayload] = Json.format
}