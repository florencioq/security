package br.com.ideos.libs.security.functions

import br.com.ideos.libs.security.exceptions.{ApiException, I18nApiException}
import org.slf4j.LoggerFactory
import pdi.jwt.{Jwt, JwtOptions}
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class LoggingActionFunction(messagesApi: MessagesApi)(implicit ec: ExecutionContext)
  extends ActionFunction[Request, Request] {

  override protected def executionContext: ExecutionContext = ec

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    implicit val messages: Messages = messagesApi.preferred(request)

    val requestLogger = LoggerFactory.getLogger("request")
    val responseLogger = LoggerFactory.getLogger("response")

    val logInfo = LogInfo(request)

    requestLogger.info(logInfo.toString)

    block(request).andThen {
      case Success(res) => responseLogger.info(logInfo.withResult(res).toString)
      case Failure(ex) => responseLogger.error(logInfo.withError(ex).toString, ex)
    }
  }


  case class LogInfo(
    uri: String,
    requestId: Option[String],
    status: Option[Int],
    userId: Option[String],
    body: Option[String],
    duration: Option[Long],
    errorMessage: Option[String],
  ) {
    private val creationTime = System.currentTimeMillis()

    def withResult(res: Result): LogInfo = this.copy(
      status = Some(res.asJava.status()),
      duration = Some(calculateTime),
    )

    def withError(ex: Throwable)(implicit messages: Messages): LogInfo = {
      val (status, message) = ex match {
        case ex: ApiException => (Some(ex.status), Some(ex.message))
        case ex: I18nApiException => (Some(ex.status), Some(ex.toApiException.message))
        case ex: Throwable => (None, Option(ex.getMessage))
      }
      this.copy(
        status = status,
        duration = Some(calculateTime),
        errorMessage = message,
      )
    }

    override def toString: String = {
      val uriString = Some(s"URI: $uri")
      val requestIdString = requestId.map(s => s"REQ.ID: $s")
      val statusString = status.map(s => s"STATUS: $s")
      val userIdString = userId.map(s => s"USER: $s")
      val bodyString = body.map(s => s"BODY: $s")
      val durationString = duration.map(s => s"DURATION: ${s}ms")
      val errorMessageString = errorMessage.map(s => s"ERROR: $s")
      Seq(
        uriString, requestIdString, statusString, userIdString, bodyString, durationString, errorMessageString
      ).flatten.mkString("{\n\t", "\n\t", "\n}\n")
    }

    private def calculateTime: Long = System.currentTimeMillis() - this.creationTime
  }

  object LogInfo {
    def apply(request: Request[_]): LogInfo = {
      val bodyFill = if (request.hasBody) Some(request.body.toString) else None

      val userIdentifier = request.cleanToken
        .map(Jwt.decodeRaw(_, JwtOptions(signature = false)))
        .flatMap(_.toOption)
        .flatMap(s => (Json.parse(s) \ "userId").toOption)
        .map(_.toString)

      new LogInfo(request.uri, request.headers.get(SecurityHeaders.RequestId), None, userIdentifier, bodyFill, None, None)
    }
  }
}
