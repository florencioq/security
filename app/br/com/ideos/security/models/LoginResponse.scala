package br.com.ideos.security.models

import play.api.libs.json.{Format, Json}

case class LoginResponse(grant: String)

object LoginResponse {
  implicit val format: Format[LoginResponse] = Json.format
}