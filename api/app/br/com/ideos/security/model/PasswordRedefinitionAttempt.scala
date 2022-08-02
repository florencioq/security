package br.com.ideos.security.model

import java.util.UUID

case class PasswordRedefinitionAttempt(userId: Long, redefinitionId: UUID)
