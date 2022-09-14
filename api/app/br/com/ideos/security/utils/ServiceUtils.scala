package br.com.ideos.security.utils

import br.com.ideos.security.utils.DbUtils.ActionRunner
import slick.dbio.Effect

import scala.concurrent.Future

object ServiceUtils {

  def ServiceCall[R, E <: Effect](block: => ActionRunner[R, E]): Future[R] = {
    block.run()
  }

}
