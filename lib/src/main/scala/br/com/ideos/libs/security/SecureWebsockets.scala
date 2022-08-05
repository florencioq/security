package br.com.ideos.libs.security

import akka.stream.scaladsl.Flow
import br.com.ideos.libs.security.PermissionRule.{IsAdmin, IsManager, OneOf}
import br.com.ideos.libs.security.exceptions.{InsufficientPermissionsException, InvalidCredentialsException}
import br.com.ideos.libs.security.model.tokens.AccessTokenPayload
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc.{RequestHeader, Result, WebSocket}

import scala.concurrent.{ExecutionContext, Future}

class SecureWebsockets(val tokenValidator: TokenValidator)(implicit ec: ExecutionContext) {

  private type WSReqBlock[In, Out] = (RequestHeader, AccessTokenPayload) => Future[Either[Result, Flow[In, Out, _]]]

  private def AuthWebsocket[In, Out](
    block: WSReqBlock[In, Out]
  )(implicit transformer: MessageFlowTransformer[In, Out]): WebSocket = WebSocket.acceptOrResult { req =>
    for {
      payload <- tokenValidator.validateToken(req.requiredToken)
      accessTokenPayload = payload match {
        case p: AccessTokenPayload => p
        case _ => throw InvalidCredentialsException()
      }
      out <- block(req, accessTokenPayload)
    } yield out
  }

  def RestrictedWebsocket[In, Out](rule: PermissionRule)(
    block: WSReqBlock[In, Out]
  )(implicit transformer: MessageFlowTransformer[In, Out]): WebSocket = AuthWebsocket[In, Out] { (req, payload) =>
    if (payload.isAdmin || rule(payload)) block(req, payload)
    else throw InsufficientPermissionsException()
  }

  def RestrictedWebsocket[In, Out](permission: String)(
    block: WSReqBlock[In, Out]
  )(implicit transformer: MessageFlowTransformer[In, Out]): WebSocket = {
    RestrictedWebsocket[In, Out](OneOf(permission))(block)
  }

  def AdminWebsocket[In, Out](
    block: WSReqBlock[In, Out]
  )(implicit transformer: MessageFlowTransformer[In, Out]): WebSocket = {
    RestrictedWebsocket[In, Out](IsAdmin)(block)
  }

  def ManagerWebsocket[In, Out](
    block: WSReqBlock[In, Out]
  )(implicit transformer: MessageFlowTransformer[In, Out]): WebSocket = {
    RestrictedWebsocket[In, Out](IsManager)(block)
  }

}
