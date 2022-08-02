package br.com.ideos.security.exceptions

import br.com.ideos.libs.security.exceptions.I18nApiException
import play.api.http.Status

case class UserNotAllowedInAppException()
  extends I18nApiException(Status.FORBIDDEN, "exceptions.userNotAllowedInApp")
