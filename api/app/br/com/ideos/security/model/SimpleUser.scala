package br.com.ideos.security.model

import play.api.libs.json.{Format, Json}

case class SimpleUser(id: Long, email: String)

object SimpleUser {
  implicit val format: Format[SimpleUser] = Json.format
}

