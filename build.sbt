
import java.nio.file.Files

name := "findItemsInEBay"

version := "0.0.1"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

fork in run := true

javaOptions in run ++= Seq(
    "-Xms512m", "-Xmx512m", "-XX:NewRatio=4"
)

val locationToExtractEBayAPI = "eBayAPI"
val eBayFindingJAR = "lib/finding.jar"
val finalLocationEBayFindingJAR = locationToExtractEBayAPI + "/" + eBayFindingJAR

lazy val getEBayFindingKitEnhaced = taskKey[Unit](
  "Get the eBay Finding Kit for Enhaced Search API and extract it to ./" + locationToExtractEBayAPI
)

getEBayFindingKitEnhaced := {

  if (Files.notExists(new File(finalLocationEBayFindingJAR).toPath)) {

    println(s"JAR file $finalLocationEBayFindingJAR does not exist: downloading and installing...")

    val eBayFindingKitURL =
      "http://developer.ebay.com/DevZone/codebase/javasdk-jaxb/FindingKitJava_1.0.zip"

    // download URL and extract only the eBayFindingJAR, not all other files in the zip archive
    // (this SBT job may fail if the above eBayFindingKitURL address becomes invalid).
    IO.unzipURL(new URL(eBayFindingKitURL), new File(locationToExtractEBayAPI),
                new ExactFilter(eBayFindingJAR))
  } else {
    println(s"JAR file $finalLocationEBayFindingJAR exists, skipping download.")
  }
}

compile in Compile <<= (compile in Compile).dependsOn(getEBayFindingKitEnhaced)

unmanagedJars in Compile  += file(finalLocationEBayFindingJAR)

// remove the [info] preffixes given by SBT
outputStrategy        :=   Some(StdoutOutput)


libraryDependencies ++= Seq(
  "log4j" % "log4j" % "1.2.17"
)

resolvers ++= Seq(
  "JBoss Repository" at "http://repository.jboss.org/nexus/content/repositories/releases/",
  "Spray Repository" at "http://repo.spray.cc/",
  "Cloudera Repository" at "https://repository.cloudera.com/artifactory/cloudera-repos/",
  "Akka Repository" at "http://repo.akka.io/releases/",
  "Apache HBase" at "https://repository.apache.org/content/repositories/releases",
  "scala-tools" at "https://oss.sonatype.org/content/groups/scala-tools",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Second Typesafe repo" at "http://repo.typesafe.com/typesafe/maven-releases/",
  "Mesosphere Public Repository" at "http://downloads.mesosphere.io/maven",
  Resolver.sonatypeRepo("public")
)

