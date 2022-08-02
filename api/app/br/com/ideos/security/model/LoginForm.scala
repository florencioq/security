package br.com.ideos.security.model

import play.api.libs.json.{Format, Json}

case class LoginForm(email: String, password: String)

object LoginForm {
  implicit val format: Format[LoginForm] = Json.format
}