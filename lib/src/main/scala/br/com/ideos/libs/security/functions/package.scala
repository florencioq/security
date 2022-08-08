package br.com.ideos.libs.security

import br.com.ideos.libs.security.exceptions.AccessTokenNotFoundException
import play.api.mvc.RequestHeader
import play.mvc.Http

package object functions {

  object SecurityHeaders {
    val RequestId = "X-Request-Id"
  }

  implicit class RichRequest(req: RequestHeader) {
    def token: Option[String] = {
      req.headers.get(Http.HeaderNames.AUTHORIZATION).orElse(req.getQueryString(Http.HeaderNames.AUTHORIZATION))
    }

    def cleanToken: Option[String] = token.map { t =>
      t.split(" ", 2) match {
        case Array(_, tokenValue) => tokenValue
        case _ => t
      }
    }

    def requiredToken: String = token.getOrElse(throw AccessTokenNotFoundException())
  }
}
