package br.com.ideos.libs.security.exceptions

import play.api.http.Status

case class AccessTokenNotFoundException()
  extends I18nApiException(Status.UNAUTHORIZED, "exceptions.tokenNotFound")