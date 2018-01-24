import sbt.Resolver
import com.lihaoyi.workbench.WorkbenchPlugin.workbenchSettings

enablePlugins(ScalaJSPlugin)
enablePlugins(WorkbenchPlugin)

workbenchSettings

scalaVersion := "2.12.4"

name    := "weather"
version := "0.1"

scalacOptions ++= Seq("-deprecation", "-feature")

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom"  % "0.9.4"
 ,"org.akka-js"  %%% "akkajsactor"  % "1.2.5.0"
 ,"org.querki"   %%% "querki-jsext" % "0.8"
)

