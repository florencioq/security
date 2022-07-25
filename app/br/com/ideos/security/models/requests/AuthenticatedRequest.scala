package br.com.ideos.security.models.requests

import br.com.ideos.security.models.token.AccessTokenPayload
import play.api.mvc.{Request, WrappedRequest}

case class AuthenticatedRequest[+A](
  request: Request[A],
  payload: AccessTokenPayload
) extends WrappedRequest(request)
