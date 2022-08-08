package br.com.ideos.libs.security

import br.com.ideos.libs.security.PermissionRule.{IsAdmin, IsManager, OneOf}
import br.com.ideos.libs.security.functions.{AuthActionRefiner, BaseActionBuilder, GrantActionRefiner, InvitationAcceptanceActionRefiner, LoggingActionFunction, PasswordRedefinitionActionRefiner, RestrictedActionFilter, ValidTokenActionRefiner}
import br.com.ideos.libs.security.model.requests.{AuthenticatedRequest, GrantRequest, InvitationAcceptanceRequest, PasswordRedefinitionRequest}
import play.api.i18n.MessagesApi
import play.api.mvc.{ActionBuilder, AnyContent, BodyParser, Request}

import scala.concurrent.ExecutionContext

class SecureActions(
  val tokenValidator: TokenValidator,
  val parsers: BodyParser[AnyContent],
  val messagesApi: MessagesApi,
)(implicit ec: ExecutionContext) {

  val SimpleAction: ActionBuilder[Request, AnyContent] = BaseActionBuilder(parsers) andThen LoggingActionFunction(messagesApi)

  private val ValidTokenAction = SimpleAction andThen ValidTokenActionRefiner(tokenValidator)

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
