/*
Lodo is a layered to-do list (Outliner)
Copyright (C) 2015 Keith Morrow.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License v3 as
published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import com.lihaoyi.workbench.Plugin._
import com.typesafe.sbt.SbtNativePackager.autoImport._
import com.typesafe.sbt.SbtNativePackager._
import sbtassembly._
import AssemblyPlugin._
import AssemblyKeys._
import com.typesafe.sbt.packager.archetypes._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import spray.revolver.RevolverPlugin._

object Settings {
  val name = "Lodo"
  val version = "0.0.4"

  val scalacOptions = Seq(
    "-Xlint",
    "-unchecked",
    "-deprecation",
    "-feature"
  )
  /** Set some basic options when running the project with Revolver */
  val jvmRuntimeOptions = Seq(
    "-Xmx1G",
    "-DDEVMODE=true"
  )
  /** Declare global dependency versions here to avoid mismatches in multi part dependencies */
  object versions {
    val scala = "2.11.5"
    val scalajsReact = "0.8.1"
  }
  /**
   * These dependencies are shared between JS and JVM projects
   * the special %%% function selects the correct version for each project
   */
  val sharedDependencies = Def.setting(Seq(
    "com.lihaoyi" %%% "autowire" % "0.2.4",
    "com.lihaoyi" %%% "upickle" % "0.2.6"
  ))
  /** Dependencies only used by the JVM project */
  val jvmDependencies = Def.setting(Seq(
    "io.spray" %% "spray-can" % "1.3.2",
    "io.spray" %% "spray-routing" % "1.3.2",
    "com.typesafe.akka" %% "akka-actor" % "2.3.6",
    "org.squeryl" %% "squeryl" % "0.9.5-7",
    "com.h2database" % "h2" % "1.2.127",
    "org.flywaydb" % "flyway-core" % "3.1"
  ))
  /** Dependencies only used by the JS project (note the use of %%% instead of %%) */
  val scalajsDependencies = Def.setting(Seq(
    "com.github.japgolly.scalajs-react" %%% "core" % versions.scalajsReact,
    "com.github.japgolly.scalajs-react" %%% "extra" % versions.scalajsReact
  ))
  /** Dependencies for external JS libs that are bundled into a single .js file according to dependency order */
  val jsDependencies = Def.setting(Seq(
    "org.webjars" % "react" % "0.12.1" / "react-with-addons.js"
  ))
  /** Same dependecies, but for production build, using minified versions */
  val jsDependenciesProduction = Def.setting(Seq(
    "org.webjars" % "react" % "0.12.1" / "react-with-addons.min.js"
  ))
}

object Build extends sbt.Build {
  val ReleaseCmd = Command.command("release") {
    state =>
      "lodoJS/fullOptJS" ::
      "lodoJS/packageJSDependencies" ::
      "stage" ::
      state
  }

  val RunProdCmd = Command.command("run-prod") {
    state =>
      "lodoJS/fullOptJS" ::
      "lodoJS/packageJSDependencies" ::
      "lodoJVM/run" ::
      state
  }

	lazy val root = project.in(file("."))
		.aggregate(lodoJS, lodoJVM)
		.settings(
      publish := {},
      publishLocal := {},
      commands ++= Seq(
        ReleaseCmd,
        RunProdCmd
      )
		)

  val productionBuild = settingKey[Boolean]("Build for production")

	lazy val lodo = crossProject.in(file("lodo"))
		.settings(
			name := Settings.name,
			version := Settings.version,
			scalaVersion := Settings.versions.scala,
			scalacOptions ++= Settings.scalacOptions,
			libraryDependencies ++= Settings.sharedDependencies.value,
      maintainer in Debian := "Keith Morrow <keith@kmorrow.ca>",
      packageSummary in Debian := "Layered Todo List Application",
      packageDescription := "Organizes an outliner into notebooks, pages and an infinite-depth series of lists and items."
		)
    .jsSettings(workbenchSettings: _*)
    .jsSettings(
      libraryDependencies ++= Settings.scalajsDependencies.value,
      productionBuild := false,
      jsDependencies ++= {if (!productionBuild.value) Settings.jsDependencies.value else Settings.jsDependenciesProduction.value},
      jsDependencies += RuntimeDOM % "test",
      skip in packageJSDependencies := false,
      testFrameworks += new TestFramework("utest.runner.Framework"),
      localUrl := ("localhost", 5001),
      bootSnippet := "Main().main();"
    )
		.jvmSettings(Revolver.settings: _*)
		.jvmSettings(
			libraryDependencies ++= Settings.jvmDependencies.value,
      unmanagedResourceDirectories in Compile += file("lodo") / "shared" / "src" / "main" / "resources",
      unmanagedResourceDirectories in Test += file("lodo") / "shared" / "src" / "test" / "resources",
      javaOptions in Revolver.reStart ++= Settings.jvmRuntimeOptions
    )

  val scalaJsOutputDir = Def.settingKey[File]("ScalaJS Output Directory")

  lazy val js2jvmSettings = Seq(fastOptJS, fullOptJS, packageJSDependencies).map { packageJSKey =>
    crossTarget in(lodoJS, Compile, packageJSKey) := scalaJsOutputDir.value
  }

  lazy val lodoJS: Project = lodo.js.settings(
    packageJSDependencies in Compile := {
      val base = (packageJSDependencies in Compile).value
      IO.copyFile(base, (classDirectory in Compile).value / "web" / "js" / base.getName)
      base
    }
  ).dependsOn(uri("git://github.com/k3d3/actuarius.git"))

  lazy val lodoJVM: Project = lodo.jvm.settings(js2jvmSettings: _*)
    .settings(assemblySettings: _*)
    .settings(
      scalaJsOutputDir := (classDirectory in Compile).value / "web" / "js",
      NativePackagerKeys.batScriptExtraDefines += "set PRODUCTION_MODE=true",
      NativePackagerKeys.bashScriptExtraDefines += "export PRODUCTION_MODE=true",
      Revolver.reStart <<= Revolver.reStart dependsOn (fastOptJS in(lodoJS, Compile)),
      mappings in Universal := {
        val universalMappings = (mappings in Universal).value
        val fatJar = (assembly in Compile).value

        val filtered = universalMappings filter {
          case (file, name) => ! name.endsWith(".jar")
        }
        // add the fat jar
        filtered :+ (fatJar -> ("lib/" + fatJar.getName))
      },
      assemblyJarName in assembly := "lodo.jar",
      NativePackagerKeys.scriptClasspath := Seq( (assemblyJarName in assembly).value ),
      commands += ReleaseCmd
  ).enablePlugins(JavaServerAppPackaging)
}
