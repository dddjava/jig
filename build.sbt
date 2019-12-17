lazy val baseName = "sig"

lazy val coreSettings = Seq(
  organization := "com.github.yoshiyoshifujii",
  scalaVersion := "2.13.1"
)

lazy val `jig-core` = (project in file("./jig-core"))
  .settings(coreSettings)
  .settings(
    name := "jig-core",
    libraryDependencies ++= Seq(
        "org.springframework"   % "spring-context"          % "5.2.1.RELEASE",
        "guru.nidi"             % "graphviz-java"           % "0.10.1",
        "ch.qos.logback"        % "logback-classic"         % "1.2.3",
        "org.slf4j"             % "slf4j-api"               % "1.7.29",
        "org.ow2.asm"           % "asm"                     % "7.2",
        "org.mybatis"           % "mybatis"                 % "3.5.3",
        "com.github.javaparser" % "java-symbol-solver-core" % "0.6.3",
        "com.github.javaparser" % "javaparser-core"         % "3.15.0",
        "org.apache.poi"        % "poi-ooxml"               % "4.1.1",
        "org.apache.poi"        % "poi"                     % "4.1.1",
        "org.junit"             % "junit-bom"               % "5.5.2" % Test,
        "org.junit.jupiter"     % "junit-jupiter-api"       % "5.5.2" % Test,
        "org.junit.platform"    % "junit-platform-commons"  % "1.5.2" % Test,
        "org.junit.jupiter"     % "junit-jupiter-params"    % "5.5.2" % Test,
        "org.junit.jupiter"     % "junit-jupiter-engine"    % "5.5.2" % Test,
        "org.assertj"           % "assertj-core"            % "3.14.0" % Test,
        "org.mockito"           % "mockito-core"            % "3.1.0" % Test,
        "org.springframework"   % "spring-test"             % "5.2.2.RELEASE" % Test excludeAll (
          ExclusionRule(organization = "org.junit")
        ),
        "org.springframework" % "spring-web" % "5.2.2.RELEASE" % Test
      )
  )

lazy val `jig-cli-scala` = (project in file("./jig-cli-scala"))
  .settings(coreSettings)
  .settings(
    name := "jig-cli-scala",
    libraryDependencies ++= Seq(
        "com.typesafe" % "config" % "1.4.0"
      )
  )
  .dependsOn(`jig-core`)

lazy val root = (project in file("."))
  .settings(coreSettings)
  .settings(
    name := s"$baseName"
  )
  .aggregate(`jig-core`, `jig-cli-scala`)
