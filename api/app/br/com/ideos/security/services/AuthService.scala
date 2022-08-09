package br.com.ideos.security.services

import br.com.ideos.libs.security.exceptions.{DefaultApiException, InvalidCredentialsException}
import br.com.ideos.libs.security.model.tokens.{AccessTokenPayload, GrantPayload, InvitationTokenPayload, PasswordRedefinitionTokenPayload}
import br.com.ideos.security.core.JwtUtils
import br.com.ideos.security.exceptions._
import br.com.ideos.security.model._
import br.com.ideos.security.model.queryparams.{Paginated, Pagination}
import br.com.ideos.security.repository.AuthRepository
import com.github.t3hnar.bcrypt._
import play.api.Configuration

import java.util.UUID
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

class AuthService(authRepository: AuthRepository, config: Configuration)(implicit ec: ExecutionContext) {

  private val grantExpiration = config.get[Duration]("security.jwt.grantExpiration")
  private val accessTokenExpiration = config.get[Duration]("security.jwt.accessExpiration")

  def login(loginForm: LoginForm): Future[String] = {
    for {
      user <- authRepository.getUserByEmail(loginForm.email).map(_.getOrElse(throw InvalidCredentialsException()))
      isPasswordValid = checkPasswordValidity(loginForm.password, user.password)
      _ = if (!isPasswordValid) throw InvalidCredentialsException()
    } yield {
        val grantPayload = GrantPayload(user.id)
        JwtUtils.generateToken(grantPayload, grantExpiration)
    }
  }

  def getAccess(userId: Long, appKey: String): Future[String] = {
    for {
      user <- authRepository.getUser(userId).map(_.getOrElse(throw UserNotFoundException()))

      userAppLink <- authRepository.getUserAppLink(userId, appKey)
      _ = userAppLink match {
        case Some(link) => if (link.disabled) throw UserDisabledException()
        case None => throw UserNotAllowedInAppException()
      }

      isAdmin <- authRepository.isAdmin(user.id)
      isManager <- authRepository.isManager(user.id, appKey)
      roles <- authRepository.permissions(user.id, appKey)
    } yield {
      val payload = AccessTokenPayload(appKey, user.id, user.email, isAdmin, isManager, roles)
      JwtUtils.generateToken(payload, accessTokenExpiration)
    }
  }

  def updatePassword(userId: Long, updatePayload: PasswordUpdatePayload): Future[Unit] = {
    for {
      user <- authRepository.getUser(userId).map(_.getOrElse(throw InvalidCredentialsException()))

      isPasswordValid = checkPasswordValidity(updatePayload.oldPassword, user.password)
      _ = if (!isPasswordValid) throw WrongOldPasswordException()

      newPassword = applyBcrypt(updatePayload.newPassword)
      _ <- authRepository.updatePassword(userId, newPassword)
    } yield ()
  }

  def updatePermissions(userId: Long, appKey: String, update: PermissionUpdatePayload): Future[Unit] = {
    authRepository.updatePermissions(userId, appKey, update)
  }

  def setUserDisabled(userId: Long, appKey: String, disabled: Boolean): Future[Unit] = {
    for {
      isAdmin <- authRepository.isAdmin(userId)
      _ = if (isAdmin) throw AdminsCantBeDisabledException()
      _ <- authRepository.setUserDisabled(userId, appKey, disabled)
    } yield ()
  }

  def getInvitationToken(email: String, appKey: String): Future[String] = {
    for {
      maybeUser <- authRepository.getUserByEmail(email)
      _ = if (maybeUser.isDefined) throw UserAlreadyExistsException()
    } yield {
      val tokenPayload = InvitationTokenPayload(email, appKey)
      JwtUtils.generateToken(tokenPayload)
    }
  }

  def createUser(email: String, password: String): Future[Unit] = {
    for {
      maybeUser <- authRepository.getUserByEmail(email)
      _ = if (maybeUser.isDefined) throw UserAlreadyExistsException()
      _ <- authRepository.createNewUser(email, applyBcrypt(password))
    } yield ()
  }

  def acceptInvitation(email: String, appKey: String): Future[Unit] = {
    for {
      user <- authRepository.getUserByEmail(email).map(_.getOrElse(throw UserNotFoundException()))
      _ <- authRepository.acceptInvitation(user.id, appKey)
    } yield ()
  }

  def getPasswordRedefinitionToken(email: String): Future[String] = {
    authRepository
      .getUserByEmail(email)
      .map(_.getOrElse(throw UserNotFoundException()))
      .map { user =>
        val tokenPayload = PasswordRedefinitionTokenPayload(user.id, UUID.randomUUID())
        JwtUtils.generateToken(tokenPayload)
      }
  }

  def redefinePassword(userId: Long, newPassword: String, redefinitionId: UUID): Future[Unit] = {
    for {
      redefinitionIdAlreadyUsed <- authRepository.isRedefinitionIdUsed(userId, redefinitionId)
      _ = if (redefinitionIdAlreadyUsed) throw RedefinitionIdAlreadyUsedException()
      _ <- authRepository.redefinePassword(userId, applyBcrypt(newPassword), redefinitionId)
    } yield ()
  }


  def listUsers(pagination: Pagination, applicationKey: String): Future[Paginated[UserInfo]] = {
    authRepository.listUsers(pagination, applicationKey)
  }

  def listSimpleUsers(ids: Seq[Long], applicationKey: String): Future[Seq[SimpleUser]] = {
    authRepository.listUsers(ids, applicationKey).map(_.map(_.simple))
  }

  def getUser(id: Long, appKey: String): Future[UserDetails] = {
    for {
      user <- authRepository.getUser(id).map(_.getOrElse(throw UserNotFoundException()))
      appLink <- authRepository.getUserAppLink(id, appKey).map(_.getOrElse(throw UserNotFoundException()))

      isAdmin <- authRepository.isAdmin(user.id)
      isManager <- authRepository.isManager(user.id, appKey)
      roles <- authRepository.permissions(user.id, appKey)
    } yield user.toUserDetails(isAdmin, isManager, appLink.disabled, roles)
  }

  def getApp(appKey: String): Future[Application] = {
    authRepository
      .getApp(appKey)
      .map(_.getOrElse(throw AppNotFoundException()))
  }

  def getRoles(appKey: String): Future[Seq[Role]] = {
    authRepository.getRoles(appKey)
  }


  private def applyBcrypt(s: String): String = {
    s.bcryptSafeBounded.getOrElse(throw new DefaultApiException())
  }

  private def checkPasswordValidity(formPassword: String, userPassword: String): Boolean = {
    formPassword.isBcryptedSafeBounded(userPassword).getOrElse(false)
  }
}
