package br.com.ideos.libs

import br.com.ideos.libs.security.exceptions.{AccessTokenNotFoundException, InsufficientPermissionsException, InvalidCredentialsException}
import br.com.ideos.libs.security.model.requests._
import br.com.ideos.libs.security.model.tokens.{AccessTokenPayload, GrantPayload, InvitationTokenPayload, PasswordRedefinitionTokenPayload}
import play.api.mvc._
import play.mvc.Http

import scala.concurrent.{ExecutionContext, Future}

package object security {
  case class ValidTokenActionRefiner(tokenValidator: TokenValidator)(implicit ec: ExecutionContext)
    extends ActionRefiner[Request, ValidTokenRequest] {

    override protected def executionContext: ExecutionContext = ec

    override protected def refine[A](request: Request[A]): Future[Either[Result, ValidTokenRequest[A]]] = {
      tokenValidator
        .validateToken(request.requiredToken)
        .map { payload =>
          Right(ValidTokenRequest(request, payload))
        }
        .recover { case ex => throw InvalidCredentialsException(Some(ex)) }
    }
  }


  case class GrantActionRefiner()(implicit ec: ExecutionContext)
    extends ActionRefiner[ValidTokenRequest, GrantRequest] {

    override protected def executionContext: ExecutionContext = ec

    override protected def refine[A](request: ValidTokenRequest[A]): Future[Either[Result, GrantRequest[A]]] = Future {
      request.payload match {
        case payload: GrantPayload => Right(GrantRequest(request, payload.userId))
        case _ => throw InvalidCredentialsException()
      }
    }
  }

  case class AuthActionRefiner()(implicit ec: ExecutionContext)
    extends ActionRefiner[ValidTokenRequest, AuthenticatedRequest] {

    override protected def executionContext: ExecutionContext = ec

    override protected def refine[A](request: ValidTokenRequest[A]): Future[Either[Result, AuthenticatedRequest[A]]] = Future {
      request.payload match {
        case payload: AccessTokenPayload => Right(AuthenticatedRequest(request, payload))
        case _ => throw InvalidCredentialsException()
      }
    }
  }

  case class InvitationAcceptanceActionRefiner()(implicit ec: ExecutionContext)
    extends ActionRefiner[ValidTokenRequest, InvitationAcceptanceRequest] {

    override protected def executionContext: ExecutionContext = ec

    override protected def refine[A](request: ValidTokenRequest[A]): Future[Either[Result, InvitationAcceptanceRequest[A]]] = Future {
      request.payload match {
        case payload: InvitationTokenPayload => Right(InvitationAcceptanceRequest(request, payload))
        case _ => throw InvalidCredentialsException()
      }
    }
  }

  case class PasswordRedefinitionActionRefiner()(implicit ec: ExecutionContext)
    extends ActionRefiner[ValidTokenRequest, PasswordRedefinitionRequest] {

    override protected def executionContext: ExecutionContext = ec

    override protected def refine[A](request: ValidTokenRequest[A]): Future[Either[Result, PasswordRedefinitionRequest[A]]] = Future {
      request.payload match {
        case payload: PasswordRedefinitionTokenPayload => Right(PasswordRedefinitionRequest(request, payload))
        case _ => throw InvalidCredentialsException()
      }
    }
  }

  case class RestrictedActionFilter(rule: PermissionRule)(implicit ec: ExecutionContext)
    extends ActionFilter[AuthenticatedRequest] {

    override protected def executionContext: ExecutionContext = ec

    override protected def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] = {
      if (request.payload.isAdmin || rule(request.payload)) Future.successful(None)
      else throw InsufficientPermissionsException()
    }
  }

  implicit class RichRequest(req: RequestHeader) {
    def requiredToken: String = {
      req.headers.get(Http.HeaderNames.AUTHORIZATION)
        .orElse(req.getQueryString(Http.HeaderNames.AUTHORIZATION))
        .getOrElse(throw AccessTokenNotFoundException())
    }
  }

}
