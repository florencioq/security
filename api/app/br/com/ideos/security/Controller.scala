package br.com.ideos.security

import br.com.ideos.libs.security.SecureActions
import br.com.ideos.security.exceptions.app.AppUrlNotFoundException
import br.com.ideos.security.exceptions.{AccessTokenNotFromAppException, UserCantAlterOwnLevelException}
import br.com.ideos.security.model.app.Application
import br.com.ideos.security.model.queryparams.Pagination
import br.com.ideos.security.model.{LoginForm, PasswordDefinitionPayload, PasswordUpdatePayload, PermissionUpdatePayload, Role}
import br.com.ideos.security.services.{AppService, AuthService, EmailService, RolesService}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

class Controller(
  authService: AuthService,
  appService: AppService,
  rolesService: RolesService,
  emailService: EmailService,
  cc: ControllerComponents,
  secureActions: SecureActions,
)(implicit ec: ExecutionContext)
  extends AbstractController(cc)
    with I18nSupport {

  import secureActions._

  def login(): Action[LoginForm] = SimpleAction(parse.json[LoginForm]).async { implicit r =>
    authService.login(r.body).map(Ok(_))
  }

  def access(appKey: String): Action[AnyContent] = GrantAction.async { implicit r =>
    authService.getAccess(r.userId, appKey).map(Ok(_))
  }

  def validate(appKey: String): Action[AnyContent] = AuthAction { implicit r =>
    if (r.payload.appKey != appKey) throw AccessTokenNotFromAppException()
    Ok(r.payload)
  }

  def forgotPassword(email: String): Action[AnyContent] = SimpleAction.async { implicit r =>
    for {
      token <- authService.getPasswordRedefinitionToken(email)
      _ = emailService.sendPasswordRedefinition(email, token)
    } yield NoContent
  }

  def updatePassword(): Action[PasswordUpdatePayload] = AuthAction(parse.json[PasswordUpdatePayload]).async { implicit r =>
    authService.updatePassword(r.payload.userId, r.body).map(_ => NoContent)
  }

  def updatePermissions(userId: Long): Action[PermissionUpdatePayload] = ManagerAction(parse.json[PermissionUpdatePayload]).async { implicit r =>
    rolesService.updatePermissions(userId, r.payload.appKey, r.body).map(_ => NoContent)
  }

  def toggleAdmin(userId: Long): Action[AnyContent] = AdminAction.async { implicit r =>
    if (r.payload.userId == userId) throw UserCantAlterOwnLevelException()
    rolesService.toggleAdmin(userId).map(_ => NoContent)
  }

  def toggleManager(userId: Long): Action[AnyContent] = ManagerAction.async { implicit r =>
    if (r.payload.userId == userId) throw UserCantAlterOwnLevelException()
    rolesService.toggleManager(userId, r.payload.appKey).map(_ => NoContent)
  }

  def disableUser(userId: Long): Action[AnyContent] = ManagerAction.async { implicit r =>
    authService.setUserDisabled(userId, r.payload.appKey, disabled = true).map(_ => NoContent)
  }

  def enableUser(userId: Long): Action[AnyContent] = ManagerAction.async { implicit r =>
    authService.setUserDisabled(userId, r.payload.appKey, disabled = false).map(_ => NoContent)
  }

  def invite(email: String): Action[AnyContent] = ManagerAction.async { implicit r =>
    for {
      token <- authService.getInvitationToken(email, r.payload.appKey)
      app <- appService.getApp(r.payload.appKey)
      webappUrl = app.webappUrl.getOrElse(throw AppUrlNotFoundException())
      _ = emailService.sendInvite(email, token, webappUrl)
    } yield NoContent
  }

  def acceptInvitation(): Action[AnyContent] = InvitationAcceptanceAction.async { implicit r =>
    authService.acceptInvitation(r.payload.email, r.payload.appKey).map(_ => NoContent)
  }

  def firstAccess(): Action[PasswordDefinitionPayload] = InvitationAcceptanceAction(parse.json[PasswordDefinitionPayload]).async { implicit r =>
    for {
      _ <- authService.createUser(r.payload.email, r.body.password)
      _ <- authService.acceptInvitation(r.payload.email, r.payload.appKey)
    } yield NoContent
  }

  def redefinePassword(): Action[PasswordDefinitionPayload] = PasswordRedefinitionAction(parse.json[PasswordDefinitionPayload]).async { implicit r =>
    authService.redefinePassword(r.payload.userId, r.body.password, r.payload.redefinitionId).map(_ => NoContent)
  }


  def listUsers(pagination: Pagination, email: Option[String]): Action[AnyContent] = ManagerAction.async { implicit r =>
    authService.listUsers(pagination, r.payload.appKey, email).map(Ok(_))
  }

  def listSimpleUsers(ids: Seq[Long]): Action[AnyContent] = AuthAction.async { implicit r =>
    authService.listSimpleUsers(ids, r.payload.appKey).map(Ok(_))
  }

  def getUser(id: Long): Action[AnyContent] = ManagerAction.async { implicit r =>
    authService.getUser(id, r.payload.appKey).map(Ok(_))
  }

  def getRoles: Action[AnyContent] = ManagerAction.async { implicit r =>
    rolesService.getRoles(r.payload.appKey).map(Ok(_))
  }

  def addRole(): Action[Role] = AdminAction(parse.json[Role]).async { implicit r =>
    rolesService.addRole(r.body).map(_ => NoContent)
  }

  def deleteRole(id: Long): Action[AnyContent] = AdminAction.async { implicit r =>
    rolesService.deleteRole(id).map(Ok(_))
  }

  def getApps: Action[AnyContent] = AuthAction.async { implicit r =>
    appService
      .listApps(if (r.payload.isAdmin) None else Some(r.payload.userId))
      .map(Ok(_))
  }

  def createApp(): Action[Application] = AdminAction(parse.json[Application]).async { implicit r =>
    appService.createApp(r.body).map(_ => NoContent)
  }

}
