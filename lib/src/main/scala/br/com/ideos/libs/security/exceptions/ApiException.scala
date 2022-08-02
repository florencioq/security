package br.com.ideos.libs.security.exceptions

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

class ApiException(
  val status: Int,
  val message: String,
  val details: Option[String] = None,
  val cause: Option[Throwable] = None
) extends Exception(message, cause.orNull) {
  override def getCause: Throwable = cause.getOrElse(super.getCause)
}

object ApiException {
  implicit val apiExceptionWrites: Writes[ApiException] = (
    (JsPath \ "status").write[Int] and
      (JsPath \ "message").write[String] and
      (JsPath \ "details").writeNullable[String] and
      (JsPath \ "trace").writeNullable[String]
    )(unlift(ApiException.unapply))

  def unapply(apiException: ApiException): Option[(Int, String, Option[String], Option[String])] =
    Some(apiException.status, apiException.message, apiException.details, apiException.cause.map(_.getStackTrace).map(formatStackTrace))

  private def formatStackTrace(stackTrace: Array[StackTraceElement]) = {
    stackTrace.map(_.toString).mkString("\n")
  }
}