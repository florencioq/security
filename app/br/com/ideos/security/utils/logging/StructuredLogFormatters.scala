package br.com.ideos.security.utils.logging

import net.logstash.logback.argument.StructuredArgument
import net.logstash.logback.argument.StructuredArguments.entries
import play.api.libs.json._
import play.api.mvc.{Request => PlayRequest, RequestHeader => PlayRequestHeader, ResponseHeader => PlayResponseHeader}

import scala.jdk.CollectionConverters._
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

object StructuredLogFormatters {

  val MissingRequestId = "missing-request-id"

  def formatRequest(requestHeader: PlayRequestHeader): StructuredArgument =
    requestHeader

  def formatResult(result: PlayResponseHeader, request: PlayRequestHeader): StructuredArgument =
    (result, request)

  def formatError(throwable: Throwable, request: PlayRequestHeader): StructuredArgument =
    (throwable, request)

  private implicit def playRequestEntries(request: PlayRequestHeader): StructuredArgument = {
    val optBody: Map[String, Object] = maybeGetPlayRequestBodyAsJsValue(request) match {
      case Some(body) => Map("body" -> body)
      case None       => Map()
    }
    entries(
      (Map(
//        "requestId" -> request.requestId.getOrElse(MissingRequestId),
        "method" -> request.method,
        "host" -> request.host,
        "uri" -> request.uri,
        "queryString" -> request.asJava.queryString(),
        "remoteAddress" -> request.remoteAddress,
        "headers" -> formatHeaders(request),
        "host" -> request.host) ++ optBody).asJava)
  }

  private def maybeGetPlayRequestBodyAsJsValue(playRequest: PlayRequestHeader): Option[Object] = {
    playRequest match {
      case e: PlayRequest[_] =>
        Some {
          val bodyAsString = e.body.toString
          val bodyAsJson = Try(Json.parse(bodyAsString)) match {
            case Success(json) => Json.prettyPrint(json)
            case Failure(exception) =>
              commonThrowabletoMap(exception) + ("raw" -> bodyAsString)
          }
          bodyAsJson
        }
      case _ => None
    }
  }

  private implicit def playResponsEntries(tuple: (PlayResponseHeader, PlayRequestHeader)): StructuredArgument =
    tuple match {
      case (response, request) =>
        entries(
          Map(
            "status" -> response.status,
            "headers" -> formatHeaders(response),
            "reason" -> response.asJava.reasonPhrase(),
            // request
            "uri" -> request.uri
//            "requestId" -> request.requestId.getOrElse(MissingRequestId)
          ).asJava)
    }

  private def commonThrowabletoMap(error: Throwable) = {
    Option(error.getCause) match {
      case Some(cause) =>
        Map(
          "message" -> error.getMessage,
          "localizedMessage" -> error.getLocalizedMessage,
          "cause" -> cause.getMessage
        )
      case None =>
        Map(
          "message" -> error.getMessage,
          "localizedMessage" -> error.getLocalizedMessage
        )
    }
  }

  private implicit def playThrowableEntries(tuple: (Throwable, PlayRequestHeader)): StructuredArgument = tuple match {
    case (error, request) =>
      val data =
        Map("uri" -> request.uri, "requestId" -> request.requestId.getOrElse(MissingRequestId))
      val map = commonThrowabletoMap(error) ++ data
      entries(map.asJava)
  }

  private def formatHeaders(request: PlayRequestHeader) = request.headers.asJava.asMap()
  private def formatHeaders(request: PlayResponseHeader) = request.headers.asJava

  implicit class RichRequestHeader(val requestHeader: PlayRequestHeader) extends AnyVal {
    def requestId: Option[String] = requestHeader.headers.get("request-id") // Request-Id comes as header always
  }

}
