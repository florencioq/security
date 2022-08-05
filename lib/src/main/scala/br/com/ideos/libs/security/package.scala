package br.com.ideos.libs

import br.com.ideos.libs.security.exceptions.{AccessTokenNotFoundException, ApiException, I18nApiException, InsufficientPermissionsException, InvalidCredentialsException}
import br.com.ideos.libs.security.model.requests._
import br.com.ideos.libs.security.model.tokens.{AccessTokenPayload, GrantPayload, InvitationTokenPayload, PasswordRedefinitionTokenPayload}
import org.slf4j.LoggerFactory
import pdi.jwt.{Jwt, JwtOptions}
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import play.mvc.Http

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

package object security {

  case class LoggingActionBuilder(
    parser: BodyParser[AnyContent],
    messagesApi: MessagesApi,
  )(implicit ec: ExecutionContext)
    extends ActionBuilder[Request, AnyContent] {

    override protected def executionContext: ExecutionContext = ec

    override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
      implicit val messages: Messages = messagesApi.preferred(request)

      val requestLogger = LoggerFactory.getLogger("request")
      val responseLogger = LoggerFactory.getLogger("response")

      val bodyFill = if (request.hasBody) s"body=${request.body.toString}" else ""

      val userIdentifier = request.cleanToken
        .map(Jwt.decodeRaw(_, JwtOptions(signature = false)))
        .flatMap(_.toOption)
        .flatMap(s => (Json.parse(s) \ "userId").toOption)
        .map(id => s" <User: $id>")
        .getOrElse("")

      requestLogger.info(s"[${request.uri}]$userIdentifier $bodyFill")
      val duration = System.currentTimeMillis()

      block(request).andThen {
        case Success(res) =>
          responseLogger.info(s"[${request.uri}] ${res.asJava.status()}$userIdentifier (${calculateTime(duration)}ms)")
        case Failure(exception) =>
          val message = exception match {
            case ex: ApiException => s"${ex.status}: ${ex.message}"
            case ex: I18nApiException => s"${ex.status}: ${ex.toApiException.message}"
            case ex: Throwable => ex.getMessage
          }
          responseLogger.error(s"[${request.uri}]$userIdentifier $message, $bodyFill (${calculateTime(duration)}ms)", exception)
      }
    }

    private def calculateTime(begin: Long): Long = {
      System.currentTimeMillis() - begin
    }
  }

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
    def token: Option[String] = {
      req.headers.get(Http.HeaderNames.AUTHORIZATION).orElse(req.getQueryString(Http.HeaderNames.AUTHORIZATION))
    }
    def cleanToken: Option[String] = token.map { t =>
      t.split(" ", 2) match {
        case Array(_, tokenValue) => tokenValue
        case _ => t
      }
    }
    def requiredToken: String = token.getOrElse(throw AccessTokenNotFoundException())
  }

}
