val scala3Version = "3.3.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "koukoku-chat-client",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "co.fs2" %% "fs2-core" % "3.9.1",
    libraryDependencies += "co.fs2" %% "fs2-io" % "3.9.1",
  )
