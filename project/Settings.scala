import com.typesafe.sbt.packager.Keys.maintainer
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
import com.typesafe.sbt.packager.docker.{Cmd, DockerChmodType, DockerPermissionStrategy}
import io.github.davidmweber.FlywayPlugin.autoImport.{flywayDriver, flywayLocations, flywayPassword, flywayUrl, flywayUser}
import sbt.Def
import sbt.Keys.version

object Settings {

  private val environment = sys.env.getOrElse("ENVIRONMENT", "dev")
  private val tagPrefix = if (environment == "prod") "" else s"$environment-"

  private val MaxRAMPercentage = 80.0
  private val JvmOpts = s"-XX:MaxRAMPercentage=$MaxRAMPercentage -XX:+UseG1GC -XshowSettings:vm"
  private val baseImage = "adoptopenjdk/openjdk11:x86_64-alpine-jdk-11.0.15_10-slim"

  val dockerSettings: Seq[sbt.Def.Setting[_]] = Seq(
    maintainer := "Ideos",
    dockerRepository := Some("registry.gitlab.com/ideos_dev/security"),
    dockerBaseImage := baseImage,
    dockerExposedPorts := Seq(9000),
    dockerUpdateLatest := false,
    dockerChmodType := DockerChmodType.UserGroupWriteExecute,
    dockerPermissionStrategy := DockerPermissionStrategy.CopyChown,

    dockerAliases := Seq(
      dockerAlias.value.withTag(Some(s"${tagPrefix}latest")),
      dockerAlias.value.withTag(Some(s"$tagPrefix${version.value}"))
    ),

    dockerCommands := dockerCommands.value.flatMap {
      case cmd @ Cmd("FROM", _) =>
        List(
          cmd,
          Cmd("ENV", "PATH=\"${PATH}:/sbin\""),
          Cmd("RUN", "apk update && apk upgrade && apk add bash && apk add curl"),
          Cmd("RUN", "mkdir -p /sbin/logs"),
          Cmd("ENV",s"JAVA_OPTS='$JvmOpts'"))
      case other => List(other)
    }
  )

  val flywaySettings: Seq[Def.Setting[_]] = Seq(
    flywayDriver := "org.postgresql.Driver",
    flywayUrl := "jdbc:postgresql://localhost:5432/security",
    flywayUser := "postgres",
    flywayPassword := "postgres",
    flywayLocations += "migration",
  )
}
