package br.com.ideos.libs.security.model.requests

import br.com.ideos.libs.security.model.tokens.TokenPayload
import play.api.mvc.{Request, WrappedRequest}

case class ValidTokenRequest[+A](request: Request[A], payload: TokenPayload) extends WrappedRequest(request)
