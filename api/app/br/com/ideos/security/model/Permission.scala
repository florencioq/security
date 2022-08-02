package br.com.ideos.security.model

import play.api.libs.json.{Format, Json}

case class Permission(user_id: Long, role_id: Long)

object Permission {
  implicit val format: Format[Permission] = Json.format
}
