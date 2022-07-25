package br.com.ideos.security.models

import play.api.libs.json.{Format, Json}

case class PasswordUpdatePayload(oldPassword: String, newPassword: String)

object PasswordUpdatePayload {
  implicit val format: Format[PasswordUpdatePayload] = Json.format
}
