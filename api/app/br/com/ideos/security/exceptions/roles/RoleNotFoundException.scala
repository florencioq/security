package br.com.ideos.security.exceptions.roles

import br.com.ideos.libs.security.exceptions.I18nApiException
import play.api.http.Status

case class RoleNotFoundException()
  extends I18nApiException(Status.NOT_FOUND, "exceptions.roleNotFound")