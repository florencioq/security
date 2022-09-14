package br.com.ideos.security.model.user

import play.api.libs.json.{Format, Json}

import java.time.Instant

case class UserInfo(
  id: Long,
  email: String,
  createdAt: Instant,
  disabled: Boolean,
) {
  def simple: SimpleUser = SimpleUser(id, email)
}

object UserInfo {
  implicit val format: Format[UserInfo] = Json.format
}

