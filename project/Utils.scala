import sbt.{Keys, Project}
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoPackage}
import sbtbuildinfo.{BuildInfoKey, BuildInfoPlugin}

object Utils {
  implicit class RichProject(val targetProject: Project) extends AnyVal {
    def withBuildInfo: Project = {
      targetProject
        .enablePlugins(BuildInfoPlugin)
        .settings(
          buildInfoKeys := Seq[BuildInfoKey](Keys.version, Keys.scalaVersion),
          buildInfoPackage := "build",
        )
    }
  }
}
