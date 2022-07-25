package br.com.ideos.security.exceptions

import play.api.http.Status

case class UserNotFoundException()
  extends I18nApiException(Status.NOT_FOUND, "exceptions.userNotFound")
