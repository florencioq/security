package br.com.ideos.security.models

import play.api.libs.json.{Format, Json}

case class Manager(userId: Long, appKey: String)

object Manager {
  implicit val format: Format[Manager] = Json.format
}