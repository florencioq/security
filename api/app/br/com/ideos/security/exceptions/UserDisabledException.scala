package br.com.ideos.security.exceptions

import br.com.ideos.libs.security.exceptions.I18nApiException
import play.api.http.Status

case class UserDisabledException()
  extends I18nApiException(Status.UNAUTHORIZED, "exceptions.userDisabled")
