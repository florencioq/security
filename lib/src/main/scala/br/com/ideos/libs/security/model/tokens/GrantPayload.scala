package br.com.ideos.libs.security.model.tokens

import play.api.libs.json.{Format, Json}

case class GrantPayload(
  userId: Long,
  override val kind: String = TokenType.Grant,
) extends TokenPayload(kind)

object GrantPayload {
  implicit val format: Format[GrantPayload] = Json.format
}
