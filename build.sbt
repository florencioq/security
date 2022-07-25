import Dependencies.{playTest, _}
import play.sbt.routes.RoutesKeys.routesImport

name := """security"""
organization := "ideos"
scalaVersion := "2.13.7"

val buildSettings = Seq(
  version := "0.1.0",
  buildInfoKeys := Seq[BuildInfoKey](version, scalaVersion),
  buildInfoPackage := "br.com.ideos.security"
)

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
)

swaggerV3 := true
swaggerDomainNameSpaces := Seq("br.com.ideos.security.models")

lazy val security = (project in file("."))
  .settings(buildSettings)
  .enablePlugins(
    PlayScala,
    BuildInfoPlugin,
    FlywayPlugin,
    SbtTwirl,
    JavaAgent,
    SwaggerPlugin,
    DockerPlugin
  )
  .settings(
    routesImport ++= Seq(
      "br.com.ideos.security.models.queryparams.Pagination",
      "br.com.ideos.security.models.queryparams.QueryOptions",
      "br.com.ideos.security.models.queryparams.QueryOptions._",
      "java.time.LocalDate",
    )
  )
