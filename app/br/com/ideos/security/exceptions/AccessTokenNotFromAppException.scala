package br.com.ideos.security.exceptions

import play.api.http.Status

case class AccessTokenNotFromAppException()
  extends I18nApiException(Status.UNAUTHORIZED, "exceptions.tokenNotFromApp")