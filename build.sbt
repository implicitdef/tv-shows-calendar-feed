name := """tv-shows-calendar-feed"""

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"

resolvers += "softprops-maven" at "http://dl.bintray.com/content/softprops/maven"

libraryDependencies += "me.lessis" %% "retry" % "0.2.0"

libraryDependencies += "com.typesafe.play" %% "play-ws" % "2.4.3"

libraryDependencies += "com.typesafe.akka" %% "akka-contrib" % "2.4.0"

