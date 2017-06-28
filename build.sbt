enablePlugins(JavaAppPackaging)

name := """error-example"""
version := "1.0"

libraryDependencies ++= {
  Seq(
        library.akkaHttp,
        library.sprayJson,

        library.enumeratum,

        library.scalaCheck          % Test,
        library.scalaTest           % Test,
        library.akkaHttpTestkit     % Test
  )
}

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val akkaHttp    = "10.0.9"
      val scalaCheck  = "1.13.4"
      val scalaTest   = "3.0.3"
    }

    val akkaHttp        = "com.typesafe.akka" %% "akka-http" % Version.akkaHttp
    val sprayJson       = "com.typesafe.akka" %% "akka-http-spray-json" % Version.akkaHttp
    val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % Version.akkaHttp

    val enumeratum      = "com.beachape" %% "enumeratum" % "1.5.10"

    val scalaCheck      = "org.scalacheck" %% "scalacheck" % Version.scalaCheck
    val scalaTest       = "org.scalatest"  %% "scalatest"  % Version.scalaTest
  }

// *****************************************************************************
// Settings
// *****************************************************************************

scalaVersion := "2.11.8"

scalacOptions := Seq(
      "-unchecked",
      "-deprecation",
      "-feature",
      "-target:jvm-1.8",
      "-encoding", "utf8")

javacOptions ++= Seq(
      "-source", "1.8",
      "-target", "1.8")

Revolver.settings
resolvers += Resolver.bintrayRepo("cakesolutions", "maven")

shellPrompt in ThisBuild := { state =>
  val project = Project.extract(state).currentRef.project
  s"[$project]> "
}

fork in run := true