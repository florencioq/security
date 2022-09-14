package br.com.ideos.security.services

import br.com.ideos.security.exceptions.app.{AppKeyNotAvailableException, AppNotFoundException}
import br.com.ideos.security.model.app.Application
import br.com.ideos.security.repository.AppRepository
import br.com.ideos.security.utils.ServiceUtils.ServiceCall

import scala.concurrent.{ExecutionContext, Future}

class AppService(appRepository: AppRepository)(implicit ec: ExecutionContext) {

  def listApps(limitingUserId: Option[Long]): Future[Seq[Application]] = ServiceCall {
    appRepository.listApps(limitingUserId)
  }

  def getApp(appKey: String): Future[Application] = ServiceCall {
    appRepository
      .getApp(appKey)
      .map(_.getOrElse(throw AppNotFoundException()))
  }

  def createApp(app: Application): Future[Unit] = ServiceCall {
    for {
      appWithThisAppKey <- appRepository.getApp(app.appKey)
      _ = if (appWithThisAppKey.isDefined) throw AppKeyNotAvailableException()
      _ <- appRepository.createApp(app)
    } yield ()
  }

}
