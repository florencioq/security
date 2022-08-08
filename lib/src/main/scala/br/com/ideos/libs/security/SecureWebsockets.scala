package br.com.ideos.libs.security

import akka.stream.scaladsl.Flow
import br.com.ideos.libs.security.PermissionRule.{IsAdmin, IsManager, OneOf}
import br.com.ideos.libs.security.exceptions.{InsufficientPermissionsException, InvalidCredentialsException}
import br.com.ideos.libs.security.functions.RichRequest
import br.com.ideos.libs.security.model.tokens.AccessTokenPayload
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc.{RequestHeader, Result, WebSocket}

import scala.concurrent.{ExecutionContext, Future}

class SecureWebsockets(val tokenValidator: TokenValidator)(implicit ec: ExecutionContext) {

  private def AuthWebsocket[In, Out](
    block: (RequestHeader, AccessTokenPayload) => Future[Either[Result, Flow[In, Out, _]]]
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

  class RestrictedWebsocket(rule: PermissionRule) {

    def apply[In, Out](
      f: RequestHeader => Flow[In, Out, _]
    )(implicit transformer: MessageFlowTransformer[In, Out]): WebSocket = accept(f)

    def accept[In, Out](
      f: RequestHeader => Flow[In, Out, _]
    )(implicit transformer: MessageFlowTransformer[In, Out]): WebSocket = {
      acceptOrResult(f.andThen(flow => Future.successful(Right(flow))))
    }

    def acceptOrResult[In, Out](
      f: RequestHeader => Future[Either[Result, Flow[In, Out, _]]]
    )(implicit transformer: MessageFlowTransformer[In, Out]): WebSocket = {
      AuthWebsocket[In, Out] { (req, payload) =>
        if (payload.isAdmin || rule(payload)) f(req)
        else throw InsufficientPermissionsException()
      }
    }
  }

  object RestrictedWebsocket {
    def apply(rule: PermissionRule): RestrictedWebsocket = new RestrictedWebsocket(rule)
    def apply(permissions: String*): RestrictedWebsocket = new RestrictedWebsocket(OneOf(permissions:_*))
  }

  val AdminWebsocket: RestrictedWebsocket = RestrictedWebsocket(IsAdmin)
  val ManagerWebsocket: RestrictedWebsocket = RestrictedWebsocket(IsManager)

}
