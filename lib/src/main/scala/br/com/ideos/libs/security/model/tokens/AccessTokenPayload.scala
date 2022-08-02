package br.com.ideos.libs.security.model.tokens

import play.api.libs.json.{Format, Json}

case class AccessTokenPayload(
  appKey: String,
  userId: Long,
  userEmail: String,
  isAdmin: Boolean,
  isManager: Boolean,
  roles: Set[String],
  override val kind: String = TokenType.Access,
) extends TokenPayload(kind)

object AccessTokenPayload {
  implicit val format: Format[AccessTokenPayload] = Json.format
}
