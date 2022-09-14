package br.com.ideos.security.repository

import br.com.ideos.security.model._
import br.com.ideos.security.utils.DbUtils.ActionRunner
import slick.dbio.Effect

trait RolesRepository {

  def getRoles(appKey: String): ActionRunner[Seq[Role], Effect.Read]

  def addRole(role: Role): ActionRunner[Int, Effect.Write]

  def deleteRole(id: Long): ActionRunner[Option[Role], Effect.Write with Effect.Read]

  def isAdmin(userId: Long): ActionRunner[Boolean, Effect.Read]

  def toggleAdmin(userId: Long): ActionRunner[Boolean, Effect.Write with Effect.Read]

  def isManager(userId: Long, appKey: String): ActionRunner[Boolean, Effect.Read]

  def toggleManager(userId: Long, appKey: String): ActionRunner[Boolean, Effect.Write with Effect.Read]

  def permissions(userId: Long, appKey: String): ActionRunner[Set[String], Effect.Read]

  def updatePermissions(userId: Long, appKey: String, update: PermissionUpdatePayload): ActionRunner[Unit, Effect.Write]

}
