package br.com.ideos.libs.security.functions

import br.com.ideos.libs.security.PermissionRule
import br.com.ideos.libs.security.exceptions.InsufficientPermissionsException
import br.com.ideos.libs.security.model.requests.AuthenticatedRequest
import play.api.mvc.{ActionFilter, Result}

import scala.concurrent.{ExecutionContext, Future}

case class RestrictedActionFilter(rule: PermissionRule)(implicit ec: ExecutionContext)
  extends ActionFilter[AuthenticatedRequest] {

  override protected def executionContext: ExecutionContext = ec

  override protected def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] = {
    if (request.payload.isAdmin || rule(request.payload)) Future.successful(None)
    else throw InsufficientPermissionsException()
  }
}
