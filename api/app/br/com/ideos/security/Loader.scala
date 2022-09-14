package br.com.ideos.security

import akka.actor.ActorSystem
import br.com.ideos.libs.security.{SecureActions, TokenValidator}
import br.com.ideos.security.core.SecurityTokenValidator
import br.com.ideos.security.repository.{AppRepository, AuthRepository, RolesRepository}
import br.com.ideos.security.repository.impl.{AppRepositoryImpl, AuthRepositoryImpl, RolesRepositoryImpl}
import br.com.ideos.security.services.{AppService, AuthService, EmailService, RolesService}
import com.softwaremill.macwire.wire
import com.typesafe.config.Config
import controllers.AssetsComponents
import play.api.ApplicationLoader.Context
import play.api._
import play.api.i18n.MessagesApi
import play.api.libs.mailer.MailerComponents
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import play.filters.cors.CORSComponents
import play.filters.headers.SecurityHeadersComponents
import router.Routes

class Loader extends ApplicationLoader {
  def load(context: Context): Application = {
    LoggerConfigurator(context.environment.classLoader)
      .foreach(_.configure(context.environment, context.initialConfiguration, Map.empty))

    context.environment.mode match {
      case Mode.Dev | Mode.Test =>
        new ApplicationModule(context).application
      case Mode.Prod =>
        new ApplicationModule(context).application
    }
  }
}

class ApplicationModule(ctx: Context)
  extends BuiltInComponentsFromContext(ctx)
    with HttpFiltersComponents
    with CORSComponents
    with SecurityHeadersComponents
    with AssetsComponents
    with MailerComponents {

  lazy val config: Config = configuration.underlying

  implicit val messages: MessagesApi = messagesApi

  override def httpFilters: Seq[EssentialFilter] = super.httpFilters ++
    Seq(corsFilter, securityHeadersFilter)

  implicit val appActorSystem: ActorSystem = actorSystem

  override def router: Router = {
    lazy val prefix = ""
    wire[Routes].withPrefix(configuration.get[String]("play.http.context"))
  }

  lazy val tokenValidator: TokenValidator = wire[SecurityTokenValidator]
  lazy val secureActions: SecureActions = wire[SecureActions]

  lazy val controller: Controller = wire[Controller]
  lazy val docsController: DocsController = wire[DocsController]


  lazy val authService: AuthService = wire[AuthService]
  lazy val appService: AppService = wire[AppService]
  lazy val rolesService: RolesService = wire[RolesService]


  lazy val authRepository: AuthRepository = wire[AuthRepositoryImpl]
  lazy val appRepository: AppRepository = wire[AppRepositoryImpl]
  lazy val rolesRepository: RolesRepository = wire[RolesRepositoryImpl]


  override lazy val httpErrorHandler: ErrorHandler = wire[ErrorHandler]

  lazy val emailService: EmailService = wire[EmailService]
}
