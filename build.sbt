Global / organization := "br.com.ideos"
Global / scalaVersion := "2.13.7"

lazy val securityApi = project in file("api")

lazy val securityLib = project in file("lib")

lazy val security = (project in file(".")).aggregate(securityApi, securityLib)

run := (securityApi / run).evaluated