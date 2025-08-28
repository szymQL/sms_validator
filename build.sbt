ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.2"

val ceVersion = "3.6.3"
val circeVersion = "0.14.14"
val quicklensVersion = "1.9.12"
val ducktapeVersion = "0.2.10"
val pureconfigVersion = "0.17.9"

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
      "com.github.pureconfig" %% "pureconfig-core" % pureconfigVersion
    ),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % munitVersion % Test
    )
  )

lazy val web = project
  .in(file("web"))
  .settings(
    name := "web"
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
