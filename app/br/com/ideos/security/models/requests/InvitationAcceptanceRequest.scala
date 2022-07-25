package br.com.ideos.security.models.requests

import br.com.ideos.security.models.token.InvitationTokenPayload
import play.api.mvc.{Request, WrappedRequest}

case class InvitationAcceptanceRequest[+A](
  request: Request[A],
  payload: InvitationTokenPayload
) extends WrappedRequest(request)
