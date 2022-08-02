package br.com.ideos.security.services

import play.api.Configuration
import play.api.i18n.Messages
import play.api.libs.mailer.{Email, MailerClient}

class EmailService(mailerClient: MailerClient, configuration: Configuration) {

  private val sender = configuration.get[String]("play.mailer.user")

  def sendInvite(email: String, token: String, webappUrl: String)(implicit messages: Messages): String = {
    mailerClient.send(Email(
      messages("mailer.invite.subject"),
      sender,
      Seq(email),
      bodyHtml = Some(s"""<html><body><a href="$webappUrl/first-access?t=$token">$webappUrl/first-access</a></body></html>""")
    ))
  }

  def sendPasswordRedefinition(email: String, token: String, webappUrl: String)(implicit messages: Messages): String = {
    mailerClient.send(Email(
      messages("mailer.passwordRecovery.subject"),
      sender,
      Seq(email),
      bodyHtml = Some(s"""<html><body><a href="$webappUrl/password-redefinition?t=$token">$webappUrl/password-redefinition</a></body></html>""")
    ))
  }

}
