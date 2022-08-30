package br.com.ideos.security.repository

import br.com.ideos.security.model.queryparams.{Paginated, Pagination}
import br.com.ideos.security.model._

import java.util.UUID
import scala.concurrent.Future

trait AuthRepository {

  def listUsers(pagination: Pagination, appKey: String, email: Option[String]): Future[Paginated[UserInfo]]

  def listUsers(ids: Seq[Long], appKey: String): Future[Seq[UserInfo]]

  def getUser(id: Long): Future[Option[User]]

  def getUserByEmail(email: String): Future[Option[User]]

  def getApp(appKey: String): Future[Option[Application]]

  def getUserAppLink(userId: Long, appKey: String): Future[Option[UserAppLink]]

  def createNewUser(email: String, password: String): Future[Int]

  def acceptInvitation(userId: Long, appKey: String): Future[Int]

  def updatePassword(userId: Long, newPassword: String): Future[Int]

  def redefinePassword(
    userId: Long,
    newPassword: String,
    redefinitionId: UUID
  ): Future[Unit]

  def isRedefinitionIdUsed(userId: Long, redefinitionId: UUID): Future[Boolean]

  def isAdmin(userId: Long): Future[Boolean]

  def toggleAdmin(userId: Long): Future[Boolean]

  def isManager(userId: Long, appKey: String): Future[Boolean]

  def toggleManager(userId: Long, appKey: String): Future[Boolean]

  def getRoles(appKey: String): Future[Seq[Role]]

  def permissions(userId: Long, appKey: String): Future[Set[String]]

  def updatePermissions(userId: Long, appKey: String, update: PermissionUpdatePayload): Future[Unit]

  def setUserDisabled(userId: Long, appKey: String, disabled: Boolean): Future[Int]

}
