import sbt._

object Dependencies {

  val postgresql = "org.postgresql" % "postgresql" % "42.3.6"
  val slick = "com.typesafe.slick" %% "slick" % "3.3.3"
  val slickHikari = "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3"
  val playJson = "com.typesafe.play" %% "play-json" % "2.9.2"
  val macwire = "com.softwaremill.macwire" %% "macros" % "2.5.7" % "provided"

  val scalaJwt = "com.github.jwt-scala" %% "jwt-core" % "9.0.6"
  val scalaBcrypt = "com.github.t3hnar" %% "scala-bcrypt" % "4.3.0"

  val scaldiPlay = "org.scaldi" %% "scaldi-play" % "0.6.1" % "provided"
  val play = "com.typesafe.play" %% "play" % "2.8.16"
  val playTest = "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % "test"

  val swaggerUI = "org.webjars" % "swagger-ui" % "4.11.1"
  val webjarLocator = "org.webjars" % "webjars-locator" % "0.42"

  val dockerClient = "com.spotify" % "docker-client" % "8.16.0"

  val logbackLogstash = "net.logstash.logback" % "logstash-logback-encoder" % "7.0.1"
  val jackson = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.1"

  val mailer = "com.typesafe.play" %% "play-mailer" % "8.0.1"
  val mailerGuice = "com.typesafe.play" %% "play-mailer-guice" % "8.0.1"

  // tests
  lazy val scalatest = "org.scalatest" %% "scalatest" % "3.2.10" % "test"
}
