package br.com.ideos.security.models.queryparams

import play.api.mvc.QueryStringBindable

case class Pagination(page: Int = 0, pageSize: Int = 10) {
  def offset: Int = page * pageSize
  def limit: Int = pageSize
}

object Pagination {
  def default: Pagination = Pagination()

  implicit def paginationQueryStringBindable(implicit intBinder: QueryStringBindable[Int]): QueryStringBindable[Pagination] =
    new QueryStringBindable[Pagination] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Pagination]] = {
        val maybePage = intBinder.bind("page", params)
        val maybePageSize = intBinder.bind("pageSize", params)
        (maybePage, maybePageSize) match {
          case (Some(Right(page)), Some(Right(pageSize))) => Some(Right(Pagination(page, pageSize)))
          case (_, Some(Right(pageSize))) => Some(Right(Pagination(pageSize = pageSize)))
          case (Some(Right(page)), _) => Some(Right(Pagination(page = page)))
          case _ => Some(Right(Pagination.default))
        }
      }
      override def unbind(key: String, pagination: Pagination): String = {
        intBinder.unbind("page", pagination.page) + "&" + intBinder.unbind("pageSize", pagination.pageSize)
      }
    }
}
