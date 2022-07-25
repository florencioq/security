package br.com.ideos.security.models.token

import play.api.libs.json._

abstract class TokenPayload(val kind: String)

object TokenPayload {
  implicit def format: Format[TokenPayload] = Format(
    json => {
      json \ "kind" match {
        case JsDefined(kind) if kind == JsString(TokenType.Grant) =>
          Json.reads[GrantPayload].reads(json)
        case JsDefined(kind) if kind == JsString(TokenType.Access) =>
          Json.reads[AccessTokenPayload].reads(json)
        case JsDefined(kind) if kind == JsString(TokenType.FirstAccess) =>
          Json.reads[InvitationTokenPayload].reads(json)
        case JsDefined(kind) if kind == JsString(TokenType.PasswordRedefinition) =>
          Json.reads[PasswordRedefinitionTokenPayload].reads(json)
        case _ => JsError("Invalid token kind")
      }
    },
    {
      case p: AccessTokenPayload => Json.writes.writes(p)
      case p: InvitationTokenPayload => Json.writes.writes(p)
      case p: PasswordRedefinitionTokenPayload => Json.writes.writes(p)
      case p: GrantPayload => Json.writes.writes(p)
    }
  )
}


