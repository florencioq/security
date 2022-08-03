package br.com.ideos.security.core

import br.com.ideos.libs.security.TokenValidator
import br.com.ideos.libs.security.model.tokens.TokenPayload

import scala.concurrent.Future

class SecurityTokenValidator extends TokenValidator {
  override def validateToken(token: String): Future[TokenPayload] = {
    Future.fromTry(JwtUtils.validateToken(token))
  }
}
