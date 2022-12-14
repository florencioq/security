package br.com.ideos.security.exceptions.app

import br.com.ideos.libs.security.exceptions.I18nApiException
import play.api.http.Status

case class AppNotFoundException()
  extends I18nApiException(Status.NOT_FOUND, "exceptions.appNotFound")
