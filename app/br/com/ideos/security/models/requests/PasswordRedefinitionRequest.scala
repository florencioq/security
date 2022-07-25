package br.com.ideos.security.models.requests

import br.com.ideos.security.models.token.PasswordRedefinitionTokenPayload
import play.api.mvc.{Request, WrappedRequest}

case class PasswordRedefinitionRequest[+A](
  request: Request[A],
  payload: PasswordRedefinitionTokenPayload
) extends WrappedRequest(request)
