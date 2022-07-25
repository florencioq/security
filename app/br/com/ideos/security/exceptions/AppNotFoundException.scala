package br.com.ideos.security.exceptions

import play.api.http.Status

case class AppNotFoundException()
  extends I18nApiException(Status.NOT_FOUND, "exceptions.appNotFound")
