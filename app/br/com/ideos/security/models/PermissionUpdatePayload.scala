package br.com.ideos.security.models

import play.api.libs.json.{Format, Json}

case class PermissionUpdatePayload(add: Set[Long], remove: Set[Long])

object PermissionUpdatePayload {
  implicit val format: Format[PermissionUpdatePayload] = Json.format
}
