package br.com.ideos.security.repository.impl

import br.com.ideos.security.model.app.Application
import br.com.ideos.security.repository.AppRepository
import br.com.ideos.security.repository.tables._
import br.com.ideos.security.utils.DbUtils.ActionRunner
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

class AppRepositoryImpl(implicit ec: ExecutionContext) extends AppRepository {

  override def listApps(limitingUserId: Option[Long]): ActionRunner[Seq[Application], Effect.Read] = ActionRunner {
    limitingUserId match {
      case Some(userId) =>
        val userLinksQuery = userAppLinksT.filter(_.userId === userId)
        applicationsT
          .join(userLinksQuery).on(_.appKey === _.appKey)
          .map(_._1)
          .result
      case None =>
        applicationsT.result
    }
  }

  override def getApp(appKey: String): ActionRunner[Option[Application], Effect.Read] = ActionRunner {
    applicationsT.filter(_.appKey === appKey).result.headOption
  }

  override def createApp(app: Application): ActionRunner[Int, Effect.Write] = ActionRunner {
    applicationsT += app
  }

}
