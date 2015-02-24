import com.lihaoyi.workbench.Plugin._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import spray.revolver.RevolverPlugin._

object LodoBuild extends Build {
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
			)
		)
    .jsSettings(workbenchSettings: _*)
    .jsSettings(
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "0.8.0",
        "com.github.japgolly.scalajs-react" %%% "core" % "0.8.0",
        "com.github.japgolly.scalajs-react" %%% "extra" % "0.8.0",
        "com.lihaoyi" %%% "scalarx" % "0.2.7",
        "com.lihaoyi" %%% "utest" % "0.3.0"
      ),
      testFrameworks += new TestFramework("utest.runner.Framework"),
      localUrl := ("localhost", 5001),
      refreshBrowsers <<= refreshBrowsers.triggeredBy(fastOptJS in Compile),
      bootSnippet := "Main().main();"
    )
		.jvmSettings(Revolver.settings: _*)
		.jvmSettings(
			libraryDependencies ++= Seq(
				"io.spray" %% "spray-can" % "1.3.2",
				"io.spray" %% "spray-routing" % "1.3.2",
				"com.typesafe.akka" %% "akka-actor" % "2.3.6"
			),
      unmanagedResourceDirectories in Compile += file("lodo") / "shared" / "src" / "main" / "resources",
      unmanagedResourceDirectories in Test += file("lodo") / "shared" / "src" / "test" / "resources",
      javaOptions in Revolver.reStart ++= Seq("-Xmx1G"),
      Revolver.enableDebugging(port = 5002, suspend = false)
    )

  val scalaJsOutputDir = Def.settingKey[File]("ScalaJS Output Directory")

  lazy val js2jvmSettings = Seq(packageScalaJSLauncher, fastOptJS, fullOptJS).map { packageJSKey =>
    crossTarget in(lodoJS, Compile, packageJSKey) := scalaJsOutputDir.value
  }

  lazy val lodoJS: Project = lodo.js.settings(
    fastOptJS in Compile := {
      val base = (fastOptJS in Compile).value
      IO.copyFile(base.data, (classDirectory in Compile).value / "web" / "js" / base.data.getName)
      IO.copyFile(base.data, (classDirectory in Compile).value / "web" / "js" / (base.data.getName + ".map"))
      base
    }
  )

  lazy val lodoJVM: Project = lodo.jvm.settings(js2jvmSettings: _*).settings(
    scalaJsOutputDir := (classDirectory in Compile).value / "web" / "js",
    compile in Compile <<= (compile in Compile) dependsOn (fastOptJS in(lodoJS, Compile))
  )
}
