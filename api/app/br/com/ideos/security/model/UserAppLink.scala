package br.com.ideos.security.model

import play.api.libs.json.{Format, Json}

import java.time.Instant

case class UserAppLink(
  userId: Long,
  appKey: String,
  disabled: Boolean = false,
  createdAt: Instant = Instant.now(),
)

object UserAppLink {
  implicit val format: Format[UserAppLink] = Json.format
}
