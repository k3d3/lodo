import com.lihaoyi.workbench.Plugin._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import spray.revolver.RevolverPlugin._
import sbtassembly.AssemblyKeys._
import sbtassembly.AssemblyPlugin._
import spray.revolver.{GlobalState, RevolverState}

object Build extends sbt.Build {
	lazy val lodoRoot = project.in(file("lodo"))
		.aggregate(lodoJS, lodoJVM)
		.settings(
			publish := {},
			publishLocal := {}
		)

	lazy val lodo = crossProject.in(file("lodo"))
		.settings(
			name := "lodo",
			version := "0.0.1",
			scalaVersion := "2.11.5",
			scalacOptions ++= Seq(
				"-Xlint",
				"-unchecked",
				"-deprecation",
				"-feature"
			),
			libraryDependencies ++= Seq(
				"com.lihaoyi" %%% "autowire" % "0.2.4",
				"com.lihaoyi" %%% "upickle" % "0.2.6"
			),
      assemblyExcludedJars in assembly := {
        (fullClasspath in Compile).value filter { _.data.getName.contains("sjs0.6_2.11") }
      }
		)
    .jsSettings(workbenchSettings: _*)
    .jsSettings(
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "0.8.0",
        "com.github.japgolly.scalajs-react" %%% "core" % "0.8.0",
        "com.github.japgolly.scalajs-react" %%% "extra" % "0.8.0"
      ),
      testFrameworks += new TestFramework("utest.runner.Framework"),
      localUrl := ("localhost", 5001),
      bootSnippet := "Main().main();",
      (assemblyExcludedJars in assembly) <<= (fullClasspath in Compile)
    )
		.jvmSettings(Revolver.settings: _*)
		.jvmSettings(
			libraryDependencies ++= Seq(
				"io.spray" %% "spray-can" % "1.3.2",
				"io.spray" %% "spray-routing" % "1.3.2",
				"com.typesafe.akka" %% "akka-actor" % "2.3.6",
        "org.squeryl" %% "squeryl" % "0.9.5-7",
        "org.slf4j" % "slf4j-simple" % "1.7.2",
        "com.h2database" % "h2" % "1.2.127",
        "postgresql" % "postgresql" % "8.4-701.jdbc4"
			),
      unmanagedResourceDirectories in Compile += file("lodo") / "shared" / "src" / "main" / "resources",
      unmanagedResourceDirectories in Test += file("lodo") / "shared" / "src" / "test" / "resources",
      javaOptions in Revolver.reStart ++= Seq(
        "-Xmx1G",
        "-DDEVMODE=true"
      )
    )

  val scalaJsOutputDir = Def.settingKey[File]("ScalaJS Output Directory")

  lazy val js2jvmSettings = Seq(packageScalaJSLauncher, fastOptJS, fullOptJS).map { packageJSKey =>
    crossTarget in(lodoJS, Compile, packageJSKey) := scalaJsOutputDir.value
  }

  lazy val buildJS = TaskKey[Unit]("buildJS", "Does things")
  lazy val buildJS2 = TaskKey[Unit]("buildJS2", "Does things")

  lazy val lodoJS: Project = lodo.js

  lazy val lodoJVM: Project = lodo.jvm.settings(js2jvmSettings: _*)
    .settings(assemblySettings: _*)
    .settings(
      scalaJsOutputDir := (classDirectory in Compile).value / "web" / "js",
      Revolver.reForkOptions <<= Revolver.reForkOptions dependsOn (fastOptJS in (lodoJS, Compile)),
      assembly <<= assembly dependsOn (fullOptJS in (lodoJS, Compile))
    )
}
