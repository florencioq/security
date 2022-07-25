package br.com.ideos.security.exceptions

import play.api.http.Status

case class UserNotAllowedInAppException()
  extends I18nApiException(Status.FORBIDDEN, "exceptions.userNotAllowedInApp")
