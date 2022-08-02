package br.com.ideos.libs.security.model.requests

import play.api.mvc.{Request, WrappedRequest}

case class GrantRequest[+A](request: Request[A], userId: Long) extends WrappedRequest(request)
