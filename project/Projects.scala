import sbt.{file, project}

object Projects {

  lazy val securityApi = (project in file("api"))

  lazy val securityLib = (project in file("lib"))

}
