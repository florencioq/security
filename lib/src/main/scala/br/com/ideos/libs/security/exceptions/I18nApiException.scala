package br.com.ideos.libs.security.exceptions

import play.api.i18n.Messages

class I18nApiException(
  val status: Int,
  val i18nKey: String,
  val i18nArgs: Seq[String] = Seq.empty,
  val message: Option[String] = None,
  val details: Option[String] = None,
  val cause: Option[Throwable] = None
) extends Throwable(message.orNull, cause.orNull) {
  def toApiException(implicit messages: Messages) = new ApiException(status, messages(i18nKey, i18nArgs), details, cause)
}

