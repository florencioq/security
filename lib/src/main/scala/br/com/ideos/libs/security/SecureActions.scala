package br.com.ideos.libs.security

import br.com.ideos.libs.security.PermissionRule.{IsAdmin, IsManager, OneOf}
import br.com.ideos.libs.security.model.requests.{AuthenticatedRequest, GrantRequest, InvitationAcceptanceRequest, PasswordRedefinitionRequest}
import play.api.mvc.{ActionBuilder, AnyContent, DefaultActionBuilder}

import scala.concurrent.ExecutionContext

class SecureActions(
  val tokenValidator: TokenValidator,
  val defaultActionBuilder: DefaultActionBuilder,
)(implicit ec: ExecutionContext) {

  private val ValidTokenAction = defaultActionBuilder andThen ValidTokenActionRefiner(tokenValidator)

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
