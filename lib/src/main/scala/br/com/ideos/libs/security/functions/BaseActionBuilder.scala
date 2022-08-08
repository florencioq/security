package br.com.ideos.libs.security.functions

import play.api.mvc._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

case class BaseActionBuilder(parser: BodyParser[AnyContent])(implicit ec: ExecutionContext)
  extends ActionBuilder[Request, AnyContent] {

  override protected def executionContext: ExecutionContext = ec

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    val requestId = UUID.randomUUID()
    val newHeaders = request.headers.add((SecurityHeaders.RequestId, requestId.toString))
    block(request.withHeaders(newHeaders))
  }
}
