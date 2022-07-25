package br.com.ideos.security

import org.webjars.WebJarAssetLocator
import play.api.http.ContentTypes
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

class DocsController(controllerComponents: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(controllerComponents) with I18nSupport {

  private val swaggerUiWebJar = "swagger-ui"
  private val webJarAssetLocator = new WebJarAssetLocator()

  def swagger(): Action[AnyContent] = Action.async {
    Future {
      Ok(modifiedSwaggerDocsIndex(getClass.getClassLoader)).as(ContentTypes.HTML)
    }
  }

  private def modifiedSwaggerDocsIndex(classLoader: ClassLoader): String = {
    val fullPath = webJarAssetLocator.getFullPath(swaggerUiWebJar, "index.html")
    val url = classLoader.getResource(fullPath)
    /**
     * Since the index.html from Swagger is relatively simple, we can get away with regular expressions.
     * I wouldn't use regexp  for any generic html content.
     */
    val fileSource = Source.fromURL(url)
    val content = fileSource.mkString
      // Update url to the swagger file
      .replaceFirst("https://petstore.swagger.io/v2/swagger.json", "/api/docs/swagger.json")
      .replaceAll("src=\"./", "src=\"./docs/")
      .replaceAll("href=\"./", "href=\"./docs/")
    fileSource.close()

    content
  }
}
