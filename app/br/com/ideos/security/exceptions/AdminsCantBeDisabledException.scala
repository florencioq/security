package br.com.ideos.security.exceptions

import play.api.http.Status

case class AdminsCantBeDisabledException()
  extends I18nApiException(Status.UNPROCESSABLE_ENTITY, "exceptions.adminsCantBeDisabled")