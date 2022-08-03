package br.com.ideos.libs.security

import br.com.ideos.libs.security.model.tokens.TokenPayload

import scala.concurrent.Future

trait TokenValidator {
  def validateToken(token: String): Future[TokenPayload]
}
