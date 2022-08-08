package br.com.ideos.libs.security.functions

import br.com.ideos.libs.security.exceptions.InvalidCredentialsException
import br.com.ideos.libs.security.model.requests.{GrantRequest, ValidTokenRequest}
import br.com.ideos.libs.security.model.tokens.GrantPayload
import play.api.mvc.{ActionRefiner, Result}

import scala.concurrent.{ExecutionContext, Future}

case class GrantActionRefiner()(implicit ec: ExecutionContext)
  extends ActionRefiner[ValidTokenRequest, GrantRequest] {

  override protected def executionContext: ExecutionContext = ec

  override protected def refine[A](request: ValidTokenRequest[A]): Future[Either[Result, GrantRequest[A]]] = Future {
    request.payload match {
      case payload: GrantPayload => Right(GrantRequest(request, payload.userId))
      case _ => throw InvalidCredentialsException()
    }
  }
}
