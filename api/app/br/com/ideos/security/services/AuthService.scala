package br.com.ideos.security.services

import br.com.ideos.libs.security.exceptions.{DefaultApiException, InvalidCredentialsException}
import br.com.ideos.libs.security.model.tokens.{AccessTokenPayload, GrantPayload, InvitationTokenPayload, PasswordRedefinitionTokenPayload}
import br.com.ideos.security.core.JwtUtils
import br.com.ideos.security.exceptions._
import br.com.ideos.security.model._
import br.com.ideos.security.model.queryparams.{Paginated, Pagination}
import br.com.ideos.security.model.user.{SimpleUser, UserDetails, UserInfo}
import br.com.ideos.security.repository.{AuthRepository, RolesRepository}
import br.com.ideos.security.utils.DbUtils.ActionRunner
import br.com.ideos.security.utils.ServiceUtils.ServiceCall
import com.github.t3hnar.bcrypt._
import play.api.Configuration

import java.util.UUID
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

class AuthService(
  authRepository: AuthRepository,
  rolesRepository: RolesRepository,
  config: Configuration,
)(implicit ec: ExecutionContext) {

  private val grantExpiration = config.get[Duration]("security.jwt.grantExpiration")
  private val accessTokenExpiration = config.get[Duration]("security.jwt.accessExpiration")

  def login(loginForm: LoginForm): Future[String] = ServiceCall {
    for {
      user <- authRepository.getUserByEmail(loginForm.email).map(_.getOrElse(throw InvalidCredentialsException()))
      isPasswordValid = checkPasswordValidity(loginForm.password, user.password)
      _ = if (!isPasswordValid) throw InvalidCredentialsException()
    } yield {
      val grantPayload = GrantPayload(user.id)
      JwtUtils.generateToken(grantPayload, grantExpiration)
    }
  }

  def getAccess(userId: Long, appKey: String): Future[String] = ServiceCall {
    for {
      user <- authRepository.getUser(userId).map(_.getOrElse(throw UserNotFoundException()))

      userAppLink <- authRepository.getUserAppLink(userId, appKey)
      _ = userAppLink match {
        case Some(link) => if (link.disabled) throw UserDisabledException()
        case None => throw UserNotAllowedInAppException()
      }

      isAdmin <- rolesRepository.isAdmin(user.id)
      isManager <- rolesRepository.isManager(user.id, appKey)
      roles <- rolesRepository.permissions(user.id, appKey)
    } yield {
      val payload = AccessTokenPayload(appKey, user.id, user.email, isAdmin, isManager, roles)
      JwtUtils.generateToken(payload, accessTokenExpiration)
    }
  }

  def updatePassword(userId: Long, updatePayload: PasswordUpdatePayload): Future[Unit] = ServiceCall {
    for {
      user <- authRepository.getUser(userId).map(_.getOrElse(throw InvalidCredentialsException()))

      isPasswordValid = checkPasswordValidity(updatePayload.oldPassword, user.password)
      _ = if (!isPasswordValid) throw WrongOldPasswordException()

      newPassword = applyBcrypt(updatePayload.newPassword)
      _ <- authRepository.updatePassword(userId, newPassword)
    } yield ()
  }

  def setUserDisabled(userId: Long, appKey: String, disabled: Boolean): Future[Unit] = ServiceCall {
    for {
      isAdmin <- rolesRepository.isAdmin(userId)
      _ = if (isAdmin) throw AdminsCantBeDisabledException()
      _ <- authRepository.setUserDisabled(userId, appKey, disabled)
    } yield ()
  }

  def getInvitationToken(email: String, appKey: String): Future[String] = ServiceCall {
    for {
      maybeUser <- authRepository.getUserByEmail(email)
      payload <- maybeUser match {
        case Some(user) => authRepository.getUserAppLink(user.id, appKey).map {
          case Some(_) => throw UserAlreadyExistsException()
          case None => InvitationTokenPayload(email, appKey, newUser = false)
        }
        case None => ActionRunner.successful(InvitationTokenPayload(email, appKey, newUser = true))
      }
    } yield JwtUtils.generateToken(payload)
  }

  def createUser(email: String, password: String): Future[Unit] = ServiceCall {
    for {
      maybeUser <- authRepository.getUserByEmail(email)
      _ = if (maybeUser.isDefined) throw UserAlreadyExistsException()
      _ <- authRepository.createNewUser(email, applyBcrypt(password))
    } yield ()
  }

  def acceptInvitation(email: String, appKey: String): Future[Unit] = ServiceCall {
    for {
      user <- authRepository.getUserByEmail(email).map(_.getOrElse(throw UserNotFoundException()))
      _ <- authRepository.acceptInvitation(user.id, appKey)
    } yield ()
  }

  def getPasswordRedefinitionToken(email: String): Future[String] = ServiceCall {
    authRepository
      .getUserByEmail(email)
      .map(_.getOrElse(throw UserNotFoundException()))
      .map { user =>
        val tokenPayload = PasswordRedefinitionTokenPayload(user.id, UUID.randomUUID())
        JwtUtils.generateToken(tokenPayload)
      }
  }

  def redefinePassword(userId: Long, newPassword: String, redefinitionId: UUID): Future[Unit] = ServiceCall {
    for {
      redefinitionIdAlreadyUsed <- authRepository.isRedefinitionIdUsed(userId, redefinitionId)
      _ = if (redefinitionIdAlreadyUsed) throw RedefinitionIdAlreadyUsedException()
      _ <- authRepository.redefinePassword(userId, applyBcrypt(newPassword), redefinitionId)
    } yield ()
  }


  def listUsers(pagination: Pagination, applicationKey: String, email: Option[String]): Future[Paginated[UserInfo]] = ServiceCall {
    for {
      res <- authRepository.listUsers(pagination, applicationKey, email)
    } yield res.transformResult { case (user, link) => user.toUserInfo(link.disabled) }
  }

  def listSimpleUsers(ids: Seq[Long], applicationKey: String): Future[Seq[SimpleUser]] = ServiceCall {
    authRepository.listUsers(ids, applicationKey).map(_.map(_.simple))
  }

  def getUser(id: Long, appKey: String): Future[UserDetails] = ServiceCall {
    for {
      user <- authRepository.getUser(id).map(_.getOrElse(throw UserNotFoundException()))
      appLink <- authRepository.getUserAppLink(id, appKey).map(_.getOrElse(throw UserNotFoundException()))

      isAdmin <- rolesRepository.isAdmin(user.id)
      isManager <- rolesRepository.isManager(user.id, appKey)
      roles <- rolesRepository.permissions(user.id, appKey)
    } yield user.toUserDetails(isAdmin, isManager, appLink.disabled, roles)
  }

  private def applyBcrypt(s: String): String = {
    s.bcryptSafeBounded.getOrElse(throw new DefaultApiException())
  }

  private def checkPasswordValidity(formPassword: String, userPassword: String): Boolean = {
    formPassword.isBcryptedSafeBounded(userPassword).getOrElse(false)
  }
}
