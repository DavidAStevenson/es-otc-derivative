name := "es-otc-derivative"

scalaVersion := "2.13.1"
def akkaVersion = "2.6.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test
)

scalacOptions in Compile ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint")

// show full stack traces and test case durations
testOptions in Test += Tests.Argument("-oDF")
logBuffered in Test := false

