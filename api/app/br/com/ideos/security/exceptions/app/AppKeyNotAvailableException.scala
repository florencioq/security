package br.com.ideos.security.exceptions.app

import br.com.ideos.libs.security.exceptions.I18nApiException
import play.api.http.Status

case class AppKeyNotAvailableException()
  extends I18nApiException(Status.UNPROCESSABLE_ENTITY, "exceptions.appKeyNotAvailable")
