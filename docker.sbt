import com.typesafe.sbt.packager.docker.Cmd
import com.typesafe.sbt.packager.docker.DockerChmodType
import com.typesafe.sbt.packager.docker.DockerPermissionStrategy

val MaxRAMPercentage = 80.0
val JvmOpts = s"-XX:MaxRAMPercentage=${MaxRAMPercentage} -XX:+UseG1GC -XshowSettings:vm"
val baseImage = "adoptopenjdk/openjdk11:x86_64-alpine-jdk-11.0.15_10-slim"

maintainer := "Ideos"
dockerRepository := Some("registry.gitlab.com/ideos.company/security")
dockerBaseImage := baseImage
dockerExposedPorts := Seq(9000)
dockerUpdateLatest := false
dockerChmodType := DockerChmodType.UserGroupWriteExecute
dockerPermissionStrategy := DockerPermissionStrategy.CopyChown

val environment = sys.env.getOrElse("ENVIRONMENT", "dev")

if (environment == "prod") {
  dockerAliases := Seq(
    dockerAlias.value.withTag(Some("latest")),
    dockerAlias.value.withTag(Some(version.value))
  )
} else {
  dockerAliases := Seq(
    dockerAlias.value.withTag(Some(s"$environment-latest")),
    dockerAlias.value.withTag(Some(s"$environment-${version.value}"))
  )
}

dockerCommands := dockerCommands.value.flatMap {
  case cmd @ Cmd("FROM", image) =>
    List(
      cmd,
      Cmd("ENV", "PATH=\"${PATH}:/sbin\""),
      Cmd("RUN", "apk update && apk upgrade && apk add bash && apk add curl"),
      Cmd("RUN", "mkdir -p /sbin/logs"),
      Cmd("ENV",s"JAVA_OPTS='${JvmOpts}'"))
  case other => List(other)
}