package br.com.ideos.security.models.queryparams

import play.api.libs.json.{Format, Json}

case class Paginated[A](page: Int,
                        pageSize: Int,
                        itemsCount: Int,
                        data: Seq[A]) {
  def transformResult[B](transform: A => B): Paginated[B] = {
    val transformedData = data.map(transform)
    copy(data = transformedData)
  }
}

object Paginated {
  implicit def format[A](implicit fmt: Format[A]): Format[Paginated[A]] = Json.format
}