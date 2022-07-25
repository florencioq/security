package br.com.ideos.security.exceptions

import play.api.http.Status

case class WrongOldPasswordException()
  extends I18nApiException(Status.UNPROCESSABLE_ENTITY, "exceptions.wrongOldPassword")
