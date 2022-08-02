package br.com.ideos.security

import br.com.ideos.libs.security.actions.Actions
import br.com.ideos.security.exceptions.{AccessTokenNotFromAppException, AppUrlNotFoundException}
import br.com.ideos.security.model.queryparams.Pagination
import br.com.ideos.security.model.{LoginForm, PasswordDefinitionPayload, PasswordUpdatePayload, PermissionUpdatePayload}
import br.com.ideos.security.services.{AuthService, EmailService}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

class Controller(
  authService: AuthService,
  emailService: EmailService,
  actions: Actions,
  override val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext)
  extends AbstractController(controllerComponents) with I18nSupport {

  import actions._

  def login(): Action[LoginForm] = Action(parse.json[LoginForm]).async { implicit r =>
    authService.login(r.body).map(Ok(_))
  }

  def access(appKey: String): Action[AnyContent] = GrantAction.async { implicit r =>
    authService.getAccess(r.userId, appKey).map(Ok(_))
  }

  def validate(appKey: String): Action[AnyContent] = AuthAction { implicit r =>
    if (r.payload.appKey != appKey) throw AccessTokenNotFromAppException()
    Ok(r.payload)
  }

  def forgotPassword(email: String, appKey: String): Action[AnyContent] = Action.async { implicit r =>
    for {
      token <- authService.getPasswordRedefinitionToken(email)
      app <- authService.getApp(appKey)
      webappUrl = app.webappUrl.getOrElse(throw AppUrlNotFoundException())
      _ = emailService.sendPasswordRedefinition(email, token, webappUrl)
    } yield NoContent
  }

  def updatePassword(): Action[PasswordUpdatePayload] = AuthAction(parse.json[PasswordUpdatePayload]).async { implicit r =>
    authService.updatePassword(r.payload.userId, r.body).map(_ => NoContent)
  }

  def updatePermissions(userId: Long): Action[PermissionUpdatePayload] = ManagerAction(parse.json[PermissionUpdatePayload]).async { implicit r =>
    authService.updatePermissions(userId, r.payload.appKey, r.body).map(_ => NoContent)
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
      app <- authService.getApp(r.payload.appKey)
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


  def listUsers(pagination: Pagination): Action[AnyContent] = ManagerAction.async { implicit r =>
    authService.listUsers(pagination, r.payload.appKey).map(Ok(_))
  }

  def getUser(id: Long): Action[AnyContent] = ManagerAction.async { implicit r =>
    authService.getUser(id, r.payload.appKey).map(Ok(_))
  }

  def getRoles: Action[AnyContent] = ManagerAction.async { implicit r =>
    authService.getRoles(r.payload.appKey).map(Ok(_))
  }

}
