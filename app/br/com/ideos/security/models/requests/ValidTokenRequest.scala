package br.com.ideos.security.models.requests

import br.com.ideos.security.models.token.TokenPayload
import play.api.mvc.{Request, WrappedRequest}

case class ValidTokenRequest[+A](request: Request[A], payload: TokenPayload) extends WrappedRequest(request)
