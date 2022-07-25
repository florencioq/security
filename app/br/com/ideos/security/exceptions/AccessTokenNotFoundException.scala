package br.com.ideos.security.exceptions

import play.api.http.Status

case class AccessTokenNotFoundException()
  extends I18nApiException(Status.UNAUTHORIZED, "exceptions.tokenNotFound")