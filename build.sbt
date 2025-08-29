ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.2"

ThisBuild / assembly / assemblyMergeStrategy := {
  case x if x.contains("module-info") => MergeStrategy.discard
  case x                              =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

val ceVersion = "3.6.3"
val circeVersion = "0.14.14"
val quicklensVersion = "1.9.12"
val ducktapeVersion = "0.2.10"
val pureconfigVersion = "0.17.9"
val sttpClientVersion = "4.0.9"
val sttpTapirVersion = "1.11.42"
val http4sVersion = "0.23.30"
val logbackVersion = "1.5.18"

val munitVersion = "1.1.1"

lazy val root = (project in file("."))
  .settings(
    name := "sms_validator"
  )
  .aggregate(commons, web)

lazy val commons = project
  .in(file("commons"))
  .settings(
    name := "commons",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % ceVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.softwaremill.quicklens" %% "quicklens" % quicklensVersion,
      "io.github.arainko" %% "ducktape" % ducktapeVersion,
      "com.github.pureconfig" %% "pureconfig-core" % pureconfigVersion,
      "com.softwaremill.sttp.client4" %% "core" % sttpClientVersion,
      "com.softwaremill.sttp.client4" %% "circe" % sttpClientVersion,
      "com.softwaremill.sttp.client4" %% "cats" % sttpClientVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-core" % sttpTapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % sttpTapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % sttpTapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % sttpTapirVersion,
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.typelevel" %% "log4cats-slf4j" % "2.7.1",
      "ch.qos.logback" % "logback-classic" % "1.5.18"
    ),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % munitVersion % Test
    )
  )

lazy val web = project
  .in(file("web"))
  .settings(
    name := "web",
    assembly / assemblyJarName := "sms-validator-web.jar"
  )
  .dependsOn(commons)

lazy val format = taskKey[Unit]("format the project")
format := Def
  .sequential(
    root / Compile / scalafmtSbt,
    root / scalafmtAll,
    commons / scalafmtAll,
    web / scalafmtAll
  )
  .value
