package br.com.ideos.security.exceptions

import play.api.http.Status

case class UserDisabledException()
  extends I18nApiException(Status.UNAUTHORIZED, "exceptions.userDisabled")
