name := "mill"
description := "simple developer editor"

version := "0.1"

scalaVersion := "2.12.6"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8", "-feature")

libraryDependencies += "org.fxmisc.richtext" % "richtextfx" % "0.9.1"
libraryDependencies += "org.controlsfx" % "controlsfx" % "9.0.0"

unmanagedJars in Compile += Attributed.blank(file("/usr/lib/jvm/openjfx/rt/lib/ext/jfxrt.jar"))

//javaFxMainClass := "mill.MillApp"
mainClass := Some("mill.MillApp")

