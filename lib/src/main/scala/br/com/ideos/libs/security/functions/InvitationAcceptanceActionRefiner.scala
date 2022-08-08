package br.com.ideos.libs.security.functions

import br.com.ideos.libs.security.exceptions.InvalidCredentialsException
import br.com.ideos.libs.security.model.requests.{InvitationAcceptanceRequest, ValidTokenRequest}
import br.com.ideos.libs.security.model.tokens.InvitationTokenPayload
import play.api.mvc.{ActionRefiner, Result}

import scala.concurrent.{ExecutionContext, Future}

case class InvitationAcceptanceActionRefiner()(implicit ec: ExecutionContext)
  extends ActionRefiner[ValidTokenRequest, InvitationAcceptanceRequest] {

  override protected def executionContext: ExecutionContext = ec

  override protected def refine[A](request: ValidTokenRequest[A]): Future[Either[Result, InvitationAcceptanceRequest[A]]] = Future {
    request.payload match {
      case payload: InvitationTokenPayload => Right(InvitationAcceptanceRequest(request, payload))
      case _ => throw InvalidCredentialsException()
    }
  }
}
