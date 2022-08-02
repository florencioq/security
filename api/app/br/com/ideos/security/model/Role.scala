package br.com.ideos.security.model

import play.api.libs.json.{Format, Json}

case class Role(id: Long, name: String, appKey: String)

object Role {
  implicit val format: Format[Role] = Json.format
}
