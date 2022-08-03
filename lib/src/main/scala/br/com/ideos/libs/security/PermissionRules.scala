package br.com.ideos.libs.security

import br.com.ideos.libs.security.model.tokens.AccessTokenPayload

object PermissionRules {
  type PermissionRule = AccessTokenPayload => Boolean

  def IsAdmin: PermissionRule = _.isAdmin

  def IsManager: PermissionRule = _.isManager

  def Never: PermissionRule = (_: AccessTokenPayload) => false

  def OneOf(permissions: String*): PermissionRule = (userPerms: AccessTokenPayload) => permissions
    .exists(userPerms.roles.contains)

  implicit class RuleUtils(rule: PermissionRule) {
    def &(other: PermissionRule): PermissionRule = (userPerms: AccessTokenPayload) => rule(userPerms) && other(userPerms)

    def |(other: PermissionRule): PermissionRule = (userPerms: AccessTokenPayload) => rule(userPerms) || other(userPerms)
  }
}
