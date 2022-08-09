package br.com.ideos.security.repository

import br.com.ideos.security.model._
import br.com.ideos.security.model.queryparams.{Paginated, Pagination}
import br.com.ideos.security.repository.tables._
import br.com.ideos.security.utils.DbUtils.{DBFutureRecover, PaginatedResultQuery}
import slick.jdbc.PostgresProfile.api._

import java.time.Instant
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AuthRepositoryImpl(implicit ec: ExecutionContext) extends AuthRepository {

  override def listUsers(pagination: Pagination, appKey: String): Future[Paginated[UserInfo]] = {
    val action = usersT
      .join(userAppLinksT.filter(_.appKey === appKey)).on(_.id === _.userId)
      .paginatedResult(pagination)

    db.run(action)
      .map(_.transformResult { case (user, link) =>
        user.toUserInfo(link.disabled)
      })
      .withGenericDBRecover
  }

  def listUsers(ids: Seq[Long], appKey: String): Future[Seq[UserInfo]] = {
    val action = usersT
      .join(userAppLinksT.filter(_.appKey === appKey)).on(_.id === _.userId)
      .filter(_._1.id inSet ids)
      .result
    db.run(action)
      .map(_.map { case (user, link) =>
        user.toUserInfo(link.disabled)
      })
      .withGenericDBRecover
  }

  override def getUser(id: Long): Future[Option[User]] = db.run(getByIdAction(id)).withGenericDBRecover

  override def getUserByEmail(email: String): Future[Option[User]] = db.run(getByEmailAction(email)).withGenericDBRecover

  override def getApp(appKey: String): Future[Option[Application]] = {
    val action = applicationsT.filter(_.appKey === appKey).result.headOption
    db.run(action).withGenericDBRecover
  }

  override def getUserAppLink(userId: Long, appKey: String): Future[Option[UserAppLink]] = {
    val action = userAppLinksT
      .filter(_.userId === userId)
      .filter(_.appKey === appKey)
      .result
      .headOption
    db.run(action).withGenericDBRecover
  }


  override def createNewUser(email: String, password: String): Future[Int] = {
    val action = usersT += User(-1, email, password, Instant.now())

    db.run(action)
      .withGenericDBRecover
  }

  override def acceptInvitation(userId: Long, appKey: String): Future[Int] = {
    val action = userAppLinksT += UserAppLink(userId, appKey)
    db.run(action).withGenericDBRecover
  }

  override def updatePassword(userId: Long, newPassword: String): Future[Int] =
    db.run(updatePasswordAction(userId, newPassword))
      .withGenericDBRecover

  override def redefinePassword(userId: Long, newPassword: String, redefinitionId: UUID): Future[Unit] = {
    val action = for {
      _ <- updatePasswordAction(userId, newPassword)
      _ <- passwordRedefinitionT += PasswordRedefinitionAttempt(userId, redefinitionId)
    } yield ()

    db.run(action.transactionally)
      .withGenericDBRecover
  }

  override def isRedefinitionIdUsed(
    userId: Long,
    redefinitionId: UUID
  ): Future[Boolean] = {
    val action = passwordRedefinitionT
      .filter(_.userId === userId)
      .filter(_.redefinitionId === redefinitionId)
      .exists
      .result

    db.run(action)
      .withGenericDBRecover
  }

  override def isAdmin(userId: Long): Future[Boolean] = db.run(isAdminAction(userId)).withGenericDBRecover

  override def isManager(userId: Long, appKey: String): Future[Boolean] = {
    val action = managersT
      .filter(_.userId === userId)
      .filter(_.appKey === appKey)
      .exists.result
    db.run(action).withGenericDBRecover
  }

  override def getRoles(appKey: String): Future[Seq[Role]] = {
    val action = rolesT.filter(_.appKey === appKey).result
    db.run(action).withGenericDBRecover
  }

  override def permissions(userId: Long, appKey: String): Future[Set[String]] = {
    val action = getPermissionsAction(userId, appKey)
    db.run(action).withGenericDBRecover
  }

  override def updatePermissions(userId: Long, appKey: String, update: PermissionUpdatePayload): Future[Unit] = {
    val action = for {
      _ <- permissionsT ++= update.add.map(Permission(userId, _))
      _ <- permissionsT
        .filter(_.userId === userId)
        .filter(_.roleId inSet update.remove)
        .delete
    } yield ()

    db.run(action.transactionally)
      .withGenericDBRecover
  }

  override def setUserDisabled(userId: Long, appKey: String, disabled: Boolean): Future[Int] = {
    val action = userAppLinksT
      .filter(_.userId === userId)
      .filter(_.appKey === appKey)
      .map(_.disabled).update(disabled)

    db.run(action)
      .withGenericDBRecover
  }

  private def getByIdAction(id: Long) =
    usersT.filter(_.id === id).result.headOption

  private def getByEmailAction(email: String) =
    usersT.filter(_.email === email).result.headOption

  private def isAdminAction(userId: Long) =
    adminsT.filter(_.userId === userId).exists.result

  private def getPermissionsAction(userId: Long, appKey: String) =
    permissionsT
      .filter(_.userId === userId)
      .join(rolesT.filter(_.appKey === appKey)).on(_.roleId === _.id)
      .map(_._2.name)
      .result
      .map(_.toSet)

  private def updatePasswordAction(userId: Long, newPassword: String) =
    usersT
      .filter(_.id === userId)
      .map(_.password)
      .update(newPassword)
}
