package br.com.ideos.libs.security.functions

import br.com.ideos.libs.security.TokenValidator
import br.com.ideos.libs.security.exceptions.InvalidCredentialsException
import br.com.ideos.libs.security.model.requests.ValidTokenRequest
import play.api.mvc.{ActionRefiner, Request, Result}

import scala.concurrent.{ExecutionContext, Future}

case class ValidTokenActionRefiner(tokenValidator: TokenValidator)(implicit ec: ExecutionContext)
  extends ActionRefiner[Request, ValidTokenRequest] {

  override protected def executionContext: ExecutionContext = ec

  override protected def refine[A](request: Request[A]): Future[Either[Result, ValidTokenRequest[A]]] = {
    tokenValidator
      .validateToken(request.requiredToken)
      .map { payload =>
        Right(ValidTokenRequest(request, payload))
      }
      .recover { case ex => throw InvalidCredentialsException(Some(ex)) }
  }
}
