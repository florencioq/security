package br.com.ideos.security.models

import play.api.libs.json.{Format, Json}

case class Application(appKey: String, name: String, webappUrl: Option[String])

object Application {
  implicit val format: Format[Application] = Json.format
}