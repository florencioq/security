package br.com.ideos.libs.security.model.requests

import br.com.ideos.libs.security.model.tokens.PasswordRedefinitionTokenPayload
import play.api.mvc.{Request, WrappedRequest}

case class PasswordRedefinitionRequest[+A](
  request: Request[A],
  payload: PasswordRedefinitionTokenPayload
) extends WrappedRequest(request)
