package br.com.ideos.security.repository.impl

import br.com.ideos.security.model._
import br.com.ideos.security.model.queryparams.{Paginated, Pagination}
import br.com.ideos.security.model.user.{User, UserAppLink}
import br.com.ideos.security.repository.AuthRepository
import br.com.ideos.security.repository.tables._
import br.com.ideos.security.utils.DbUtils.{ActionRunner, PaginatedResultQuery}
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api._

import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext

class AuthRepositoryImpl(implicit ec: ExecutionContext) extends AuthRepository {

  override def listUsers(
    pagination: Pagination,
    appKey: String,
    email: Option[String]
  ): ActionRunner[Paginated[(User, UserAppLink)], Effect.Read] = ActionRunner {
    usersT
      .filter(u => email.fold(true.bind)(e => u.email.toLowerCase.like(s"%${e.toLowerCase}%")))
      .join(userAppLinksT.filter(_.appKey === appKey)).on(_.id === _.userId)
      .paginatedResult(pagination)
  }

  def listUsers(ids: Seq[Long], appKey: String): ActionRunner[Seq[User], Effect.Read] = ActionRunner {
    usersT
      .join(userAppLinksT.filter(_.appKey === appKey)).on(_.id === _.userId)
      .filterNot(_._2.disabled)
      .filter(_._1.id inSet ids)
      .map(_._1)
      .result
  }

  override def getUser(id: Long): ActionRunner[Option[User], Effect.Read] = ActionRunner {
    getByIdAction(id)
  }

  override def getUserByEmail(email: String): ActionRunner[Option[User], Effect.Read] = ActionRunner {
    getByEmailAction(email)
  }

  override def getUserAppLink(userId: Long, appKey: String): ActionRunner[Option[UserAppLink], Effect.Read] = ActionRunner {
    userAppLinksT
      .filter(_.userId === userId)
      .filter(_.appKey === appKey)
      .result
      .headOption
  }


  override def createNewUser(email: String, password: String): ActionRunner[Int, Effect.Write] = ActionRunner {
    usersT += User(-1, email, password, Instant.now())
  }

  override def acceptInvitation(userId: Long, appKey: String): ActionRunner[Int, Effect.Write] = ActionRunner {
    userAppLinksT += UserAppLink(userId, appKey)
  }

  override def updatePassword(userId: Long, newPassword: String): ActionRunner[Int, Effect.Write] = ActionRunner {
    updatePasswordAction(userId, newPassword)
  }

  override def redefinePassword(userId: Long, newPassword: String, redefinitionId: UUID): ActionRunner[Unit, Effect.Write] = ActionRunner {
    for {
      _ <- updatePasswordAction(userId, newPassword)
      _ <- passwordRedefinitionT += PasswordRedefinitionAttempt(userId, redefinitionId)
    } yield ()
  }

  override def isRedefinitionIdUsed(
    userId: Long,
    redefinitionId: UUID
  ): ActionRunner[Boolean, Effect.Read] = ActionRunner {
    passwordRedefinitionT
      .filter(_.userId === userId)
      .filter(_.redefinitionId === redefinitionId)
      .exists
      .result
  }

  override def setUserDisabled(userId: Long, appKey: String, disabled: Boolean): ActionRunner[Int, Effect.Write] = ActionRunner {
    userAppLinksT
      .filter(_.userId === userId)
      .filter(_.appKey === appKey)
      .map(_.disabled).update(disabled)
  }

  private def getByIdAction(id: Long) =
    usersT.filter(_.id === id).result.headOption

  private def getByEmailAction(email: String) =
    usersT.filter(_.email === email).result.headOption

  private def updatePasswordAction(userId: Long, newPassword: String) =
    usersT
      .filter(_.id === userId)
      .map(_.password)
      .update(newPassword)
}
