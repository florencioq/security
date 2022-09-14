package br.com.ideos.security.repository

import slick.lifted.TableQuery

package object tables {
  val SchemaName: Option[String] = Some("security")

  val usersT = TableQuery[UsersTable]

  val applicationsT = TableQuery[ApplicationsTable]

  val userAppLinksT = TableQuery[UserAppLinksTable]

  val adminsT = TableQuery[AdminsTable]

  val managersT = TableQuery[ManagersTable]

  val rolesT = TableQuery[RolesTable]

  val permissionsT = TableQuery[PermissionsTable]

  val passwordRedefinitionT = TableQuery[PasswordRedefinitionTable]
}
