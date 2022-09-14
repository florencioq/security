package br.com.ideos.security.repository

import br.com.ideos.security.model.app.Application
import br.com.ideos.security.utils.DbUtils.ActionRunner
import slick.dbio.Effect

trait AppRepository {

  def listApps(limitingUserId: Option[Long]): ActionRunner[Seq[Application], Effect.Read]

  def getApp(appKey: String): ActionRunner[Option[Application], Effect.Read]

  def createApp(app: Application): ActionRunner[Int, Effect.Write]

}
