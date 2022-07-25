package br.com.ideos.security.exceptions

import play.mvc.Http.Status.INTERNAL_SERVER_ERROR

class DefaultApiException(
  override val details: Option[String] = None,
  override val cause: Option[Throwable] = None
) extends I18nApiException(
    status = INTERNAL_SERVER_ERROR,
    i18nKey = "api.exceptions.default",
    details = details,
    cause = cause
  )
