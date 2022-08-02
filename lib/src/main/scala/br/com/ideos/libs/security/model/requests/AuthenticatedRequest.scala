package br.com.ideos.libs.security.model.requests

import br.com.ideos.libs.security.model.tokens.AccessTokenPayload
import play.api.mvc.{Request, WrappedRequest}

case class AuthenticatedRequest[+A](
  request: Request[A],
  payload: AccessTokenPayload
) extends WrappedRequest(request)
