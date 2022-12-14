package br.com.ideos.security.utils

import br.com.ideos.libs.security.exceptions.I18nApiException
import br.com.ideos.security.model.queryparams.{Paginated, Pagination}
import org.postgresql.util.PSQLException
import play.api.http.Status
import slick.dbio.{DBIOAction, Effect, NoStream}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Query

import java.sql.BatchUpdateException
import scala.concurrent.{ExecutionContext, Future}

object DbUtils {

  val db = Database.forConfig("db.default") // Not sure if it's the best place for this

  class ActionRunner[+R, -E <: Effect](block: => DBIOAction[R, NoStream, E])(implicit ec: ExecutionContext) {
    def run(): Future[R] = db.run(block.transactionally).withGenericDBRecover

    def action: DBIOAction[R, NoStream, E] = block

    def map[R2](f: R => R2): ActionRunner[R2, E with Effect] = flatMap(r => ActionRunner.successful(f(r)))

    def flatMap[S, E2 <: Effect](f: R => ActionRunner[S, E2]): ActionRunner[S, E with E2] = ActionRunner {
      for {
        r <- this.action
        s <- f(r).action
      } yield s
    }
  }

  object ActionRunner {
    def apply[R, E <: Effect](block: => DBIOAction[R, NoStream, E])(implicit ec: ExecutionContext): ActionRunner[R, E] = {
      new ActionRunner[R, E](block)
    }

    def successful[R](r: R)(implicit ec: ExecutionContext): ActionRunner[R, Effect] = ActionRunner {
      DBIO.successful(r)
    }
  }

  implicit class PaginatedResultQuery[+E, U](query: Query[E, U, Seq])
    (implicit ec: ExecutionContext) {
    def paginatedResult(pagination: Pagination): DBIOAction[Paginated[U], NoStream, Effect.Read] = {
      for {
        result <- query.drop(pagination.offset).take(pagination.limit).result
        count <- query.length.result
      } yield Paginated(
        page = pagination.page,
        pageSize = pagination.pageSize,
        itemsCount = count,
        data = result
      )
    }
  }

  implicit class DBFutureRecover[T](val future: Future[T]) extends AnyVal {

    def withGenericDBRecover(implicit exc: ExecutionContext): Future[T] = future
      .recover {
        case ex: BatchUpdateException => throw ex.getCause
        case ex => throw ex
      }
      .recover {
        case sqlException: PSQLException => handlePQSLExceptionBasedOnErrorCode(sqlException)
        case ex => throw ex
      }
  }

  private val postgresDetailsPattern = "\\((.*)\\)=\\((.*)\\)".r

  def getConstraintViolated(throwable: Throwable): Option[(String, String)] = {
    postgresDetailsPattern
      .findFirstMatchIn(throwable.getMessage)
      .map(m => (m.group(1), m.group(2)))
  }

  private def handlePQSLExceptionBasedOnErrorCode[T](exception: PSQLException): T = {
    val errorCode = exception.getServerErrorMessage.getSQLState
    errorCode match {
      case PostgresErrorCodes.ForeignKeyViolation => handleForeignKeyException(exception)
      case PostgresErrorCodes.UniqueViolation => handleUniqueException(exception)
      case _ => throw exception
    }
  }

  private def handleForeignKeyException[T](exception: PSQLException): T = {
    val table = beautifyName(exception.getServerErrorMessage.getTable)
    val details = exception.getServerErrorMessage.getDetail
    throw new I18nApiException(
      Status.UNPROCESSABLE_ENTITY,
      "repository.exceptions.foreignKeyViolated",
      details = Some(s"[Table: $table]\n$details"),
      cause = Some(exception)
    )
  }

  private def handleUniqueException[T](exception: PSQLException): T = {
    val constraint = exception.getServerErrorMessage.getConstraint
    val details = exception.getServerErrorMessage.getDetail
    throw new I18nApiException(
      Status.UNPROCESSABLE_ENTITY,
      "repository.exceptions.uniqueConstraintViolated",
      details = Some(s"[Constraint: $constraint]\n$details"),
      cause = Some(exception)
    )
  }

  private def beautifyName(name: String): String = {
    name.replaceAll("_", " ")
  }

  private object PostgresErrorCodes {

    // Reference
    // https://www.postgresql.org/docs/9.4/errcodes-appendix.html

    val UniqueViolation = "23505"
    val ForeignKeyViolation = "23503"
//    val InvalidTableDefinition = "42P16"

  }

}
