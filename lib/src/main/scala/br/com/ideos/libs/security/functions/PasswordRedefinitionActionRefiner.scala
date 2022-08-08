package br.com.ideos.libs.security.functions

import br.com.ideos.libs.security.exceptions.InvalidCredentialsException
import br.com.ideos.libs.security.model.requests.{PasswordRedefinitionRequest, ValidTokenRequest}
import br.com.ideos.libs.security.model.tokens.PasswordRedefinitionTokenPayload
import play.api.mvc.{ActionRefiner, Result}

import scala.concurrent.{ExecutionContext, Future}

case class PasswordRedefinitionActionRefiner()(implicit ec: ExecutionContext)
  extends ActionRefiner[ValidTokenRequest, PasswordRedefinitionRequest] {

  override protected def executionContext: ExecutionContext = ec

  override protected def refine[A](request: ValidTokenRequest[A]): Future[Either[Result, PasswordRedefinitionRequest[A]]] = Future {
    request.payload match {
      case payload: PasswordRedefinitionTokenPayload => Right(PasswordRedefinitionRequest(request, payload))
      case _ => throw InvalidCredentialsException()
    }
  }
}
