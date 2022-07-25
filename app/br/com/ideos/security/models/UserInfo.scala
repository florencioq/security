package br.com.ideos.security.models

import play.api.libs.json.{Format, Json}

import java.time.Instant

case class UserInfo(
  id: Long,
  email: String,
  createdAt: Instant,
  disabled: Boolean,
)

object UserInfo {
  implicit val format: Format[UserInfo] = Json.format
}

