package br.com.ideos.libs.security.utils

import br.com.ideos.libs.security.model.tokens.TokenPayload
import com.typesafe.config.ConfigFactory
import pdi.jwt.JwtAlgorithm.HS256
import pdi.jwt.{Jwt, JwtClaim, JwtOptions}
import play.api.Configuration
import play.api.libs.json.Json

import java.time.Clock
import scala.concurrent.duration.{Duration, DurationInt}
import scala.util.{Failure, Success, Try}

object JwtUtils {
  private val jwtAlgorithm = HS256

  private val authScheme = "Bearer"
  private val config: Configuration = Configuration(ConfigFactory.load)
  private val jwtSecret = config.get[String]("security.jwt.secret")

  def generateToken(payload: TokenPayload, expiration: Duration = 1.days): String = {
    implicit val clock: Clock = Clock.systemUTC
    val jwtClaim = JwtClaim(Json.toJson(payload).toString()).issuedNow.expiresIn(expiration.toSeconds)
    Jwt.encode(jwtClaim, jwtSecret, jwtAlgorithm)
  }

  def validateToken(token: String): Try[TokenPayload] = {
    val splitToken = token.split(" ", 2)
    splitToken match {
      case Array(scheme, tokenValue) if scheme == authScheme && Jwt.isValid(tokenValue, jwtSecret, Seq(jwtAlgorithm)) =>
        Jwt.decodeRaw(tokenValue, JwtOptions(signature = false))
          .map(Json.parse)
          .map(Json.fromJson[TokenPayload])
          .flatMap(_.asOpt match {
            case Some(claims) => Success(claims)
            case None => Failure(new RuntimeException("invalid token"))
          })
      case _ => Failure(new RuntimeException("invalid token"))
    }
  }
}
