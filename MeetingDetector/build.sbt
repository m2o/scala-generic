name := "MeetingDetector"
 
version := "1.0"
 
scalaVersion := "2.11.8"
 
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += Resolver.sonatypeRepo("public")

libraryDependencies += "com.github.scopt" %% "scopt" % "3.5.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"

mainClass := Some("meetingdetector.MeetingDetectorMain")
