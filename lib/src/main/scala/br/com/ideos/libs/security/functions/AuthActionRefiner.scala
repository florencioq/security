package br.com.ideos.libs.security.functions

import br.com.ideos.libs.security.exceptions.InvalidCredentialsException
import br.com.ideos.libs.security.model.requests.{AuthenticatedRequest, ValidTokenRequest}
import br.com.ideos.libs.security.model.tokens.AccessTokenPayload
import play.api.mvc.{ActionRefiner, Result}

import scala.concurrent.{ExecutionContext, Future}

case class AuthActionRefiner()(implicit ec: ExecutionContext)
  extends ActionRefiner[ValidTokenRequest, AuthenticatedRequest] {

  override protected def executionContext: ExecutionContext = ec

  override protected def refine[A](request: ValidTokenRequest[A]): Future[Either[Result, AuthenticatedRequest[A]]] = Future {
    request.payload match {
      case payload: AccessTokenPayload => Right(AuthenticatedRequest(request, payload))
      case _ => throw InvalidCredentialsException()
    }
  }
}
