package br.com.ideos.security.exceptions

import play.api.http.Status

case class InsufficientPermissionsException()
  extends I18nApiException(Status.FORBIDDEN, "exceptions.insufficientPermissions")
