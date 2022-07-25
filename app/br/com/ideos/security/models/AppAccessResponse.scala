package br.com.ideos.security.models

import play.api.libs.json.{Format, Json}

case class AppAccessResponse(
  userId: Long,
  accessToken: String,
  isAdmin: Boolean,
  isManager: Boolean,
  permissions: Set[String],
)

object AppAccessResponse {
  implicit val format: Format[AppAccessResponse] = Json.format
}