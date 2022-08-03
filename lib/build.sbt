import Dependencies._
import Utils.RichProject

lazy val securityLib = (project in file("."))
  .withBuildInfo
  .enablePlugins(JavaAgent)
  .settings(
    name := """security-lib""",
    version := "0.2.0",
    libraryDependencies ++= Seq(
      scalaJwt,
      play,
      playJson
    )
  )
