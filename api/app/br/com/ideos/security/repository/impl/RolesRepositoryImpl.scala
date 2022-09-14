package br.com.ideos.security.repository.impl

import br.com.ideos.security.model._
import br.com.ideos.security.repository.RolesRepository
import br.com.ideos.security.repository.tables._
import br.com.ideos.security.utils.DbUtils.ActionRunner
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

class RolesRepositoryImpl(implicit ec: ExecutionContext) extends RolesRepository {

  override def getRoles(appKey: String): ActionRunner[Seq[Role], Effect.Read] = ActionRunner {
    rolesT.filter(_.appKey === appKey).result
  }

  override def addRole(role: Role): ActionRunner[Int, Effect.Write] = ActionRunner {
    rolesT += role
  }

  override def deleteRole(id: Long): ActionRunner[Option[Role], Effect.Write with Effect.Read] = ActionRunner {
    val query = rolesT.filter(_.id === id)
    for {
      role <- query.result.headOption
      _ <- query.delete
    } yield role
  }

  override def isAdmin(userId: Long): ActionRunner[Boolean, Effect.Read] = ActionRunner {
    isAdminAction(userId)
  }

  override def toggleAdmin(userId: Long): ActionRunner[Boolean, Effect.Write with Effect.Read] = ActionRunner {
    val query = adminsT.filter(_.userId === userId)
    for {
      exists <- query.exists.result
      res <- if (exists) query.delete else adminsT += AdminRef(userId)
    } yield res > 0
  }

  override def isManager(userId: Long, appKey: String): ActionRunner[Boolean, Effect.Read] = ActionRunner {
    managersT
      .filter(_.userId === userId)
      .filter(_.appKey === appKey)
      .exists.result
  }

  override def toggleManager(userId: Long, appKey: String): ActionRunner[Boolean, Effect.Read with Effect.Write] = ActionRunner {
    val query = managersT
      .filter(_.appKey === appKey)
      .filter(_.userId === userId)

    for {
      exists <- query.exists.result
      res <- if (exists) query.delete else managersT += Manager(userId, appKey)
    } yield res > 0
  }

  override def permissions(userId: Long, appKey: String): ActionRunner[Set[String], Effect.Read] = ActionRunner {
    getPermissionsAction(userId, appKey)
  }

  override def updatePermissions(userId: Long, appKey: String, update: PermissionUpdatePayload): ActionRunner[Unit, Effect.Write] = ActionRunner {
    for {
      _ <- permissionsT ++= update.add.map(Permission(userId, _))
      _ <- permissionsT
        .filter(_.userId === userId)
        .filter(_.roleId inSet update.remove)
        .delete
    } yield ()
  }

  private def isAdminAction(userId: Long) =
    adminsT.filter(_.userId === userId).exists.result

  private def getPermissionsAction(userId: Long, appKey: String) =
    permissionsT
      .filter(_.userId === userId)
      .join(rolesT.filter(_.appKey === appKey)).on(_.roleId === _.id)
      .map(_._2.name)
      .result
      .map(_.toSet)

}
