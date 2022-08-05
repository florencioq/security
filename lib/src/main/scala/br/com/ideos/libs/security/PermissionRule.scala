package br.com.ideos.libs.security

import br.com.ideos.libs.security.model.tokens.AccessTokenPayload

trait PermissionRule extends (AccessTokenPayload => Boolean) {
  def &(other: PermissionRule): PermissionRule = (userPerms: AccessTokenPayload) => this(userPerms) && other(userPerms)

  def |(other: PermissionRule): PermissionRule = (userPerms: AccessTokenPayload) => this(userPerms) || other(userPerms)
}

object PermissionRule {
  def IsAdmin: PermissionRule = _.isAdmin

  def IsManager: PermissionRule = _.isManager

  def Never: PermissionRule = (_: AccessTokenPayload) => false

  def OneOf(permissions: String*): PermissionRule = (userPerms: AccessTokenPayload) => permissions
    .exists(userPerms.roles.contains)
}
