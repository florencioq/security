package br.com.ideos.security.model.queryparams

import play.api.libs.json.{Format, JsNull, JsReadable, Json}
import play.api.mvc.QueryStringBindable
import br.com.ideos.security.optionFormat

import scala.reflect.runtime.universe._
import scala.util.Try

case class QueryFilter(field: String, comparator: FilterComparator, value: String) {
  override def toString: String = s"$field:${comparator.toString}:$value"

  private def isValueNull: Boolean = value == "None"

  private def isValueString[V](implicit typeTag: TypeTag[V]): Boolean = {
    (Try(value.toBoolean).isFailure && Try(value.toDouble).isFailure && !isValueNull) || typeOf[V] =:= typeOf[String]
  }

  private def getStringifiedValue[V](implicit typeTag: TypeTag[V]): String =
    if (isValueString[V]) Literal(Constant(value)).toString else value

  private def getJsonValue[V](implicit typeTag: TypeTag[V]): JsReadable = {
    if (isValueNull) {
      JsNull
    } else {
      val stringifiedValue = getStringifiedValue[V]
      Json.parse(s"""{"key": $stringifiedValue }""") \ "key"
    }
  }

  def getValue[V](implicit reads: Format[V], typeTag: TypeTag[V]): Option[V] =
    getJsonValue[V].asOpt[V]

  def getNullableValue[V](implicit reads: Format[V], typeTag: TypeTag[V]): Option[V] =
    getJsonValue[V].asOpt[Option[V]].flatten
}

case class QueryOrdering(field: String, order: OrderingType) {
  override def toString: String = s"$field:${order.toString}"
}

case class QueryOptions(orderBy: Option[QueryOrdering],
                        filters: Iterable[QueryFilter],
                        pagination: Pagination) {
  def defaultOrderBy(defaultQueryOrdering: QueryOrdering): QueryOptions = {
    val newOrderBy = orderBy match {
      case None => Some(defaultQueryOrdering)
      case definedOrderBy => definedOrderBy
    }

    this.copy(orderBy = newOrderBy)
  }
}


object QueryOptions {

  private def parseOrdering(orderingStr: String): Option[QueryOrdering] = {
    val strParts = orderingStr.split(":", 2)
    strParts match {
      case Array(field, orderingTypeStr) =>
        OrderingType(orderingTypeStr).map { orderingType =>
          QueryOrdering(field, orderingType)
        }
      case _ => None
    }
  }

  private def parseFilters(filtersStr: Iterable[String]): Iterable[QueryFilter] = {
    filtersStr.flatMap { filterStr =>
      val strParts = filterStr.split(":", 3)
      strParts match {
        case Array(field, comparatorStr, value) =>
          FilterComparator(comparatorStr).map { comparator =>
            QueryFilter(field, comparator, value)
          }
        case _ => None
      }
    }
  }

  implicit def orderingQueryStringBindable(implicit stringBinder: QueryStringBindable[String])
  : QueryStringBindable[QueryOrdering] =
    new QueryStringBindable[QueryOrdering] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, QueryOrdering]] = {
        val maybeOrderingStr = stringBinder.bind("orderBy", params).flatMap {
          case Right(orderingStr) => Some(orderingStr)
          case Left(_) => None
        }
        val ordering = maybeOrderingStr.flatMap(parseOrdering)
        ordering.map(Right(_))
      }

      override def unbind(key: String, queryOrdering: QueryOrdering): String = {
          stringBinder.unbind("orderBy", queryOrdering.toString)
      }
    }

  implicit def queryFiltersStringBindable(implicit stringListBinder: QueryStringBindable[List[String]],
                                          stringBinder: QueryStringBindable[String],
                                          intBinder: QueryStringBindable[Int])
  : QueryStringBindable[List[QueryFilter]] =
    new QueryStringBindable[List[QueryFilter]] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, List[QueryFilter]]] = {
        val filtersStr = stringListBinder.bind("filters", params).map {
          case Right(filtersStr) => filtersStr
          case Left(_) => List.empty
        }.getOrElse(List.empty)

        val queryFilters = parseFilters(filtersStr).toList
        Some(Right(queryFilters))
      }

      override def unbind(key: String, queryFilters: List[QueryFilter]): String = {
        stringListBinder.unbind("filters", queryFilters.map(_.toString))
      }
    }

  implicit def queryOptionsStringBindable(implicit orderingBinder: QueryStringBindable[QueryOrdering],
                                          filtersBinder: QueryStringBindable[List[QueryFilter]],
                                          paginationBinder: QueryStringBindable[Pagination])
  : QueryStringBindable[QueryOptions] =
    new QueryStringBindable[QueryOptions] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, QueryOptions]] = {
        val pagination = paginationBinder.bind(key, params)
          .flatMap(_.fold(_ => None, Some(_))).getOrElse(Pagination.default)

        val maybeOrdering = orderingBinder.bind(key, params).flatMap(_.fold(_ => None, Some(_)))

        val filters = filtersBinder.bind(key, params).flatMap(_.fold(_ => None, Some(_))).getOrElse(List.empty)

        val queryOptions = QueryOptions(
          orderBy = maybeOrdering,
          filters = filters,
          pagination = pagination
        )
        Some(Right(queryOptions))
      }

      override def unbind(key: String, queryOptions: QueryOptions): String = {
        paginationBinder.unbind(key, queryOptions.pagination) + "&" +
          filtersBinder.unbind(key, queryOptions.filters.toList) +
          queryOptions.orderBy.map(orderingBinder.unbind(key, _)).map(a => s"&$a").getOrElse("")
      }
    }
}