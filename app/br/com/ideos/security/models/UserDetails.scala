package br.com.ideos.security.models

import play.api.libs.json.{Format, Json}

import java.time.Instant

case class UserDetails(
  id: Long,
  email: String,
  createdAt: Instant,
  disabled: Boolean,
  isAdmin: Boolean,
  isManager: Boolean,
  permissions: Set[String],
)

object UserDetails {
  implicit val format: Format[UserDetails] = Json.format
}
