package br.com.ideos.security

import br.com.ideos.libs.security.exceptions.{ApiException, DefaultApiException, I18nApiException}
import play.api.http.HttpErrorHandler
import play.api.i18n._
import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Result, Results}

import scala.concurrent.Future

class ErrorHandler(implicit val messagesApi: MessagesApi) extends HttpErrorHandler {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    handleError(new ApiException(status = statusCode, message = message))
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] =  {
    implicit val messages: Messages = messagesApi.preferred(request)
    val apiException = exception match {
      case e: I18nApiException => e.toApiException
      case e: ApiException => e
      case e => new DefaultApiException(Some(e.getMessage), Some(e)).toApiException
    }

    apiException.status match {
      case s if s >= 400 && s < 500 => handleWarning(apiException)
      case _ => handleError(apiException)
    }
  }

  private def handleError(apiException: ApiException): Future[Result] =
    Future.successful(transformToResult(apiException))

  private def handleWarning(apiException: ApiException): Future[Result] =
    Future.successful(transformToResult(apiException))

  private def transformToResult(apiException: ApiException) = {
    Results
      .Status(apiException.status)
      .apply(Json.toJson(apiException)(ApiException.apiExceptionWrites))
  }

}