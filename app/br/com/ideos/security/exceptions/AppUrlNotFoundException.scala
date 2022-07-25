package br.com.ideos.security.exceptions

import play.api.http.Status

case class AppUrlNotFoundException()
  extends I18nApiException(Status.NOT_FOUND, "exceptions.appUrlNotFound")
