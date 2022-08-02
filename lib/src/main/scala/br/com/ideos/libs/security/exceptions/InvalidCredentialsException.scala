package br.com.ideos.libs.security.exceptions

import play.api.http.Status

case class InvalidCredentialsException()
  extends I18nApiException(Status.UNAUTHORIZED, "exceptions.invalidCredentials")
