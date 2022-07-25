package br.com.ideos.security.exceptions

import play.api.http.Status

case class RedefinitionIdAlreadyUsedException()
  extends I18nApiException(Status.UNPROCESSABLE_ENTITY, "exceptions.redefinitionIdUsed")
