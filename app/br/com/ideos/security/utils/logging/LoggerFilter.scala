package br.com.ideos.security.utils.logging

import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import br.com.ideos.security.exceptions.ApiException
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class LoggerFilter(logger: LoggerTransformer)(
    implicit val mat: Materializer,
    implicit val executionContext: ExecutionContext)
    extends Filter {

  // this apply logs request WITH BODY, because from it we can get the request body
  override def apply(next: EssentialAction): EssentialAction =
    super.apply(EssentialAction { requestHeader: RequestHeader =>
      val flow = Flow[ByteString].map { body =>
        logger(requestHeader)
        body
      }
      next(requestHeader).through(flow)
    }: EssentialAction)

  // this apply logs request WITH NO BODY, response and error
  override def apply(next: RequestHeader => Future[Result])(request: RequestHeader): Future[Result] = {
    // helpers
    def now: Long = System.currentTimeMillis()
    // core
    // avoid duplicated logs of requests with body
    // requests with body are logged in another apply
    if (!request.hasBody) logger(request)
    val start = now
    val responseFuture = next(request)
    responseFuture.onComplete { result =>
      val duration = now - start
      result match {
        case Success(response) => logger(response.header, request, duration)
        case Failure(throwable) => throwable match {
          case t: ApiException => logger(t, request, duration)
          case _ => logger(throwable, request, duration)
        }
      }
    }
    responseFuture
  }

}
