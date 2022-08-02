package br.com.ideos

import akka.util.ByteString
import play.api.http.{ContentTypes, Writeable}
import play.api.libs.json._

package object security {
  implicit def jsonWritable[T](implicit writes: Writes[T]): Writeable[T] = {
    Writeable(
      t => ByteString.fromArray(Json.toBytes(writes.writes(t))),
      Some(ContentTypes.JSON)
    )
  }

  implicit def optionFormat[T: Format]: Format[Option[T]] = new Format[Option[T]]{
    override def reads(json: JsValue): JsResult[Option[T]] = json.validateOpt[T]

    override def writes(o: Option[T]): JsValue = o match {
      case Some(t) => implicitly[Writes[T]].writes(t)
      case None => JsNull
    }
  }
}
