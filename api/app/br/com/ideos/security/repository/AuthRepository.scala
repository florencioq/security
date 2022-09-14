package br.com.ideos.security.repository

import br.com.ideos.security.model.queryparams.{Paginated, Pagination}
import br.com.ideos.security.model.user.{User, UserAppLink}
import br.com.ideos.security.utils.DbUtils.ActionRunner
import slick.dbio.Effect

import java.util.UUID

trait AuthRepository {

  def listUsers(pagination: Pagination, appKey: String, email: Option[String]): ActionRunner[Paginated[(User, UserAppLink)], Effect.Read]

  def listUsers(ids: Seq[Long], appKey: String): ActionRunner[Seq[User], Effect.Read]

  def getUser(id: Long): ActionRunner[Option[User], Effect.Read]

  def getUserByEmail(email: String): ActionRunner[Option[User], Effect.Read]

  def getUserAppLink(userId: Long, appKey: String): ActionRunner[Option[UserAppLink], Effect.Read]

  def createNewUser(email: String, password: String): ActionRunner[Int, Effect.Write]

  def acceptInvitation(userId: Long, appKey: String): ActionRunner[Int, Effect.Write]

  def updatePassword(userId: Long, newPassword: String): ActionRunner[Int, Effect.Write]

  def redefinePassword(
    userId: Long,
    newPassword: String,
    redefinitionId: UUID
  ): ActionRunner[Unit, Effect.Write]

  def isRedefinitionIdUsed(userId: Long, redefinitionId: UUID): ActionRunner[Boolean, Effect.Read]

  def setUserDisabled(userId: Long, appKey: String, disabled: Boolean): ActionRunner[Int, Effect.Write]

}
