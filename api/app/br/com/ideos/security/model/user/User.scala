package br.com.ideos.security.model.user

import play.api.libs.json.{Format, Json}

import java.time.Instant

case class User(
  id: Long,
  email: String,
  password: String,
  createdAt: Instant,
) {
  def toUserInfo(disabled: Boolean): UserInfo = UserInfo(id, email, createdAt, disabled)

  def toUserDetails(isAdmin: Boolean, isManager:  Boolean, disabled: Boolean, permissions: Set[String]): UserDetails =
    UserDetails(id, email, createdAt, disabled, isAdmin, isManager, permissions)

  def simple: SimpleUser = SimpleUser(id, email)
}

object User {
  implicit val format: Format[User] = Json.format
}
