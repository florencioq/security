package br.com.ideos.security.models

package object queryparams {

  sealed trait OrderingType

  object OrderingType {
    case object Asc extends OrderingType {
      override def toString: String = "asc"
    }
    case object Desc extends OrderingType {
      override def toString: String = "desc"
    }

    def apply(str: String): Option[OrderingType] = str.stripMargin.toLowerCase match {
      case "asc" => Some(Asc)
      case "desc" => Some(Desc)
      case _ => None
    }
  }

  sealed trait FilterComparator

  object FilterComparator {
    case object Equals extends FilterComparator {
      override def toString: String = "eq"
    }
    case object NotEquals extends FilterComparator {
      override def toString: String = "neq"
    }
    case object LessThan extends FilterComparator {
      override def toString: String = "lt"
    }
    case object GreaterThan extends FilterComparator {
      override def toString: String = "gt"
    }
    case object LessThanOrEqual extends FilterComparator {
      override def toString: String = "lte"
    }
    case object GreaterThanOrEqual extends FilterComparator {
      override def toString: String = "gte"
    }
    case object Contains extends FilterComparator {
      override def toString: String = "contains"
    }

    val comparators = Seq(
      Equals, NotEquals, LessThan, GreaterThan, LessThanOrEqual, GreaterThan, GreaterThanOrEqual, Contains
    )

    def apply(str: String): Option[FilterComparator] = comparators.find(_.toString == str.stripMargin.toLowerCase)
  }
}
