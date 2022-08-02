package br.com.ideos.security.utils.logging

import br.com.ideos.libs.security.exceptions.ApiException
import build.BuildInfo
import net.logstash.logback.argument.StructuredArgument
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.LoggerFactory
import play.api.mvc.{RequestHeader => PlayRequestHeader, ResponseHeader => PlayResponseHeader}
import play.mvc.Http.Status

import scala.concurrent.{ExecutionContext, Future}

class LoggerTransformer(implicit executionContext: ExecutionContext) {

  private val buildInfoJson = keyValue("version", BuildInfo.version)

  private def durationKeyValue(duration: Long) = keyValue("duration", duration)

  private val requestLogger = LoggerFactory.getLogger("request")
  private val responseLogger = LoggerFactory.getLogger("response")

  // Play Loggers

  def apply(request: PlayRequestHeader): Unit = Future {
    val structuredArgument = StructuredLogFormatters.formatRequest(request)
    // IP GET / STATUS
    val message = messageFromRequest(request)
    logRequest(message, Seq(structuredArgument))
  }

  def apply(response: PlayResponseHeader, request: PlayRequestHeader, duration: Long): Unit = Future {
    val structuredArgument = StructuredLogFormatters.formatResult(response, request)
    val message = messageFromResponse(request, response)
    logResponse(message, Seq(structuredArgument, durationKeyValue(duration)))
  }

  def apply(throwable: Throwable, request: PlayRequestHeader, duration: Long): Unit = Future {
    val structuredArgument = StructuredLogFormatters.formatError(throwable, request)
    val message = throwable.getMessage
    logError(message, throwable, Seq(structuredArgument, durationKeyValue(duration)))
  }

  def apply(apiException: ApiException, request: PlayRequestHeader, duration: Long): Unit = Future {
    val structuredArgument = StructuredLogFormatters.formatError(apiException, request)
    val message = apiException.getMessage
    logApiException(message, apiException, Seq(structuredArgument))
  }

  def logWebSocketTermination(request: PlayRequestHeader, duration: Long): Unit = {
    val response = new PlayResponseHeader(Status.OK, Map.empty, Some("WebSocket's flow finishes normally"))
    val structuredArgument = StructuredLogFormatters.formatResult(response, request)
    val message = s"WebSocket's flow finishes normally in $duration ms"
    logResponse(message, Seq(structuredArgument, durationKeyValue(duration)))
  }

  private def logRequest(message: String, arguments: Seq[StructuredArgument] = Seq.empty): Unit = {
    val args = arguments ++ Seq(buildInfoJson)
    requestLogger.info(message, args: _*)
  }

  private def logResponse(message: String, arguments: Seq[StructuredArgument] = Seq.empty): Unit = {
    val args = arguments ++ Seq(buildInfoJson)
    responseLogger.info(message, args: _*)
  }

  private def logWarn(message: String, throwable: Throwable, arguments: Seq[AnyRef] = Seq.empty): Unit = {
    val args = arguments ++ Seq(buildInfoJson, throwable)
    responseLogger.warn(message, args: _*)
  }

  private def logError(message: String, throwable: Throwable, arguments: Seq[AnyRef] = Seq.empty): Unit = {
    val args = arguments ++ Seq(buildInfoJson, throwable)
    responseLogger.error(message, args: _*)
  }

  private def logApiException(
      message: String,
      apiException: ApiException,
      arguments: Seq[StructuredArgument] = Seq.empty): Unit = {
    if (apiException.status >= 500) logError(message, apiException, arguments)
    else {

      logWarn(message, apiException, arguments)
    }
  }

  private def messageFromRequest(request: PlayRequestHeader): String = {
    messageFromRequestOrResponse(
      LogMessage(request.remoteAddress, request.method, request.uri, request.headers.get("User-Agent"), status = None))
  }

  private def messageFromResponse(request: PlayRequestHeader, response: PlayResponseHeader): String = {
    messageFromRequestOrResponse(
      LogMessage(
        request.remoteAddress,
        request.method,
        request.uri,
        request.headers.get("User-Agent"),
        status = Some(response.status)))
  }

  private case class LogMessage(
      remoteAddress: String,
      method: String,
      uri: String,
      userAgent: Option[String],
      status: Option[Int])

  private def messageFromRequestOrResponse(logMessage: LogMessage) = {
    s"${logMessage.remoteAddress} ${logMessage.method} ${logMessage.uri} ${logMessage.status.fold("") { s =>
      s.toString
    }} ${logMessage.userAgent.getOrElse("-")}"
  }

}
