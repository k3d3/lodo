import com.lihaoyi.workbench.Plugin._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import sbtassembly._
import spray.revolver.RevolverPlugin._
import sbtassembly.AssemblyKeys._
import sbtassembly.AssemblyPlugin._

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
      // sbt-assembly can't handle JSDEPENDENCIES, so remove the ScalaJS side of each shared lib
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
      // sbt-assembly can't handle JSDEPENDENCIES, so remove them all
      (assemblyExcludedJars in assembly) <<= (fullClasspath in Compile)
    )
		.jvmSettings(Revolver.settings: _*)
		.jvmSettings(
			libraryDependencies ++= Seq(
				"io.spray" %% "spray-can" % "1.3.2",
				"io.spray" %% "spray-routing" % "1.3.2",
				"com.typesafe.akka" %% "akka-actor" % "2.3.6",
        "org.squeryl" %% "squeryl" % "0.9.5-7",
        "com.h2database" % "h2" % "1.2.127"
			),
      unmanagedResourceDirectories in Compile += file("lodo") / "shared" / "src" / "main" / "resources",
      unmanagedResourceDirectories in Test += file("lodo") / "shared" / "src" / "test" / "resources",
      javaOptions in Revolver.reStart ++= Seq(
        "-Xmx1G",
        "-DDEVMODE=true" // Setting this to false will use fullOpt javascript, which likely won't exist
      )
    )

  val scalaJsOutputDir = Def.settingKey[File]("ScalaJS Output Directory")

  lazy val js2jvmSettings = Seq(packageScalaJSLauncher, fastOptJS, fullOptJS).map { packageJSKey =>
    crossTarget in(lodoJS, Compile, packageJSKey) := scalaJsOutputDir.value
  }

  lazy val lodoJS: Project = lodo.js

  lazy val lodoJVM: Project = lodo.jvm.settings(js2jvmSettings: _*)
    .settings(assemblySettings: _*)
    .settings(
      scalaJsOutputDir := (classDirectory in Compile).value / "web" / "js",
      Revolver.reForkOptions <<= Revolver.reForkOptions dependsOn (fastOptJS in (lodoJS, Compile)),
      assembly <<= assembly dependsOn (fullOptJS in (lodoJS, Compile)),
      assemblyMergeStrategy in assembly := {
        case "web/js/lodo-opt.js.map" |
             "web/js/lodo-fastopt.js.map" |
             "web/js/lodo-fastopt.js" |
             "web/index.html" =>
          MergeStrategy.discard
        case nsc if nsc.startsWith("scala/tools/nsc") =>
          MergeStrategy.discard // Not needed and takes up the most space in the jar
        case par if par.startsWith("scala/collection/parallel") =>
          MergeStrategy.discard // Save some more space
        case scalap if scalap.startsWith("scala/tools/scalap") =>
          MergeStrategy.discard
        case x =>
          (assemblyMergeStrategy in assembly).value(x)
      },
      target in assembly := file(".")
    )
}
