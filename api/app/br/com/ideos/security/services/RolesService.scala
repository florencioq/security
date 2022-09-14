package br.com.ideos.security.services

import br.com.ideos.security.exceptions.roles.RoleNotFoundException
import br.com.ideos.security.model._
import br.com.ideos.security.repository.RolesRepository
import br.com.ideos.security.utils.ServiceUtils.ServiceCall
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

class RolesService(
  rolesRepository: RolesRepository,
  config: Configuration,
)(implicit ec: ExecutionContext) {

  def getRoles(appKey: String): Future[Seq[Role]] = ServiceCall {
    rolesRepository.getRoles(appKey)
  }

  def addRole(role: Role): Future[Int] = ServiceCall {
    rolesRepository.addRole(role)
  }

  def deleteRole(id: Long): Future[Role] = ServiceCall {
    rolesRepository
      .deleteRole(id)
      .map(_.getOrElse(throw RoleNotFoundException()))
  }

  def updatePermissions(userId: Long, appKey: String, update: PermissionUpdatePayload): Future[Unit] = ServiceCall {
    rolesRepository.updatePermissions(userId, appKey, update)
  }

  def toggleAdmin(userId: Long): Future[Boolean] = ServiceCall {
    rolesRepository.toggleAdmin(userId)
  }

  def toggleManager(userId: Long, appKey: String): Future[Boolean] = ServiceCall {
    rolesRepository.toggleManager(userId, appKey)
  }

}
