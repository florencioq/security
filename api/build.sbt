import Dependencies.{playTest, _}
import Utils.RichProject

lazy val securityApi = (project in file("."))
  .dependsOn(Projects.securityLib)
  .withBuildInfo
  .enablePlugins(
    PlayScala,
    FlywayPlugin,
    SbtTwirl,
    JavaAgent,
    SwaggerPlugin,
    DockerPlugin
  )
  .settings(Settings.flywaySettings)
  .settings(Settings.dockerSettings)
  .settings(
    name := """security-api""",
    version := "0.1.0",

    routesImport ++= Seq(
      "br.com.ideos.security.model.queryparams.Pagination",
      "br.com.ideos.security.model.queryparams.QueryOptions",
      "br.com.ideos.security.model.queryparams.QueryOptions._",
      "java.time.LocalDate"
    ),

    libraryDependencies ++= Seq(
      postgresql,
      slick,
      slickHikari,
      playJson,
      macwire,
      scalaJwt,
      scalaBcrypt,
      scaldiPlay,
      playTest,
      logbackLogstash,
      jackson,
      swaggerUI,
      webjarLocator,
      dockerClient,
      mailer,
      mailerGuice,
      scalatest
    ),

    swaggerV3 := true,
    swaggerDomainNameSpaces := Seq(
      "br.com.ideos.security.model",
      "br.com.ideos.libs.security.model"
    )

  )
