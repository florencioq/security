package br.com.ideos.libs.security.actions

import br.com.ideos.libs.security.actions.PermissionRules.{IsAdmin, IsManager, OneOf, PermissionRule}
import br.com.ideos.libs.security.model.requests.{AuthenticatedRequest, GrantRequest, InvitationAcceptanceRequest, PasswordRedefinitionRequest}
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.mvc.{ActionBuilder, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

class Actions(
  val config: Configuration,
  val cc: ControllerComponents
)(implicit ec: ExecutionContext, messagesApi: MessagesApi) {

  private val ValidTokenAction = cc.actionBuilder andThen ValidTokenActionRefiner(config)

  val GrantAction: ActionBuilder[GrantRequest, AnyContent] =
    ValidTokenAction andThen GrantActionRefiner()

  val AuthAction: ActionBuilder[AuthenticatedRequest, AnyContent] =
    ValidTokenAction andThen AuthActionRefiner()

  val InvitationAcceptanceAction: ActionBuilder[InvitationAcceptanceRequest, AnyContent] =
    ValidTokenAction andThen InvitationAcceptanceActionRefiner()

  val PasswordRedefinitionAction: ActionBuilder[PasswordRedefinitionRequest, AnyContent] =
    ValidTokenAction andThen PasswordRedefinitionActionRefiner()

  def RestrictedAction(permission: String): ActionBuilder[AuthenticatedRequest, AnyContent] = {
    RestrictedAction(OneOf(permission))
  }

  def RestrictedAction(rule: PermissionRule): ActionBuilder[AuthenticatedRequest, AnyContent] = {
    AuthAction andThen RestrictedActionFilter(rule)
  }

  val AdminAction: ActionBuilder[AuthenticatedRequest, AnyContent] = RestrictedAction(IsAdmin)
  val ManagerAction: ActionBuilder[AuthenticatedRequest, AnyContent] = RestrictedAction(IsManager)

}
