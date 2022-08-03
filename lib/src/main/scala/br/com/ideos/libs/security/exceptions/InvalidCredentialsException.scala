package br.com.ideos.libs.security.exceptions

import play.api.http.Status

case class InvalidCredentialsException(override val cause: Option[Throwable] = None)
  extends I18nApiException(Status.UNAUTHORIZED, "exceptions.invalidCredentials", cause = cause)
