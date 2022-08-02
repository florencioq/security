package br.com.ideos.security.model

import play.api.libs.json.{Format, Json}

case class PasswordDefinitionPayload(password: String)

object PasswordDefinitionPayload {
  implicit val format: Format[PasswordDefinitionPayload] = Json.format
}
