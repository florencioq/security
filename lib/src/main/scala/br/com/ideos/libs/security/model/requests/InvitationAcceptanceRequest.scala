package br.com.ideos.libs.security.model.requests

import br.com.ideos.libs.security.model.tokens.InvitationTokenPayload
import play.api.mvc.{Request, WrappedRequest}

case class InvitationAcceptanceRequest[+A](
  request: Request[A],
  payload: InvitationTokenPayload
) extends WrappedRequest(request)
