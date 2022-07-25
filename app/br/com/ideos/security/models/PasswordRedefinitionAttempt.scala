package br.com.ideos.security.models

import java.util.UUID

case class PasswordRedefinitionAttempt(userId: Long, redefinitionId: UUID)
