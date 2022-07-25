package br.com.ideos.security

import akka.actor.ActorSystem
import br.com.ideos.security.actions.Actions
import br.com.ideos.security.repository.{AuthRepository, AuthRepositoryImpl}
import br.com.ideos.security.services.{AuthService, EmailService}
import br.com.ideos.security.utils.logging.{LoggerFilter, LoggerTransformer}
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

  def loggerTransformer: LoggerTransformer = wire[LoggerTransformer]
  lazy val loggerFilter: LoggerFilter = wire[LoggerFilter]

  override def httpFilters: Seq[EssentialFilter] = super.httpFilters ++
    Seq(corsFilter, securityHeadersFilter, loggerFilter)

  implicit val appActorSystem: ActorSystem = actorSystem

  override def router: Router = {
    lazy val prefix = ""
    wire[Routes].withPrefix(configuration.get[String]("play.http.context"))
  }

  lazy val actions: Actions = wire[Actions]

  lazy val controller: Controller = wire[Controller]
  lazy val docsController: DocsController = wire[DocsController]

  lazy val service: AuthService = wire[AuthService]
  lazy val repository: AuthRepository = wire[AuthRepositoryImpl]

  override lazy val httpErrorHandler: ErrorHandler = wire[ErrorHandler]

  lazy val emailService: EmailService = wire[EmailService]
}
