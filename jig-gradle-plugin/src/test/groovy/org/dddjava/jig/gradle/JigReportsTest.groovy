package org.dddjava.jig.gradle

import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class JigReportsTest extends Specification {

    @TempDir
    Path testProjectDir

    @IgnoreIf({ jvm.java17 && data.gradleVersion == '6.9.3' })
    def "run jigReports"() {
        given:
        testProjectDir.resolve('settings.gradle') << "rootProject.name = 'hello-jig'"
        testProjectDir.resolve(buildScript.name) << buildScript.content
        writeSampleJavaSource()

        when:
        def result = GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(testProjectDir.toFile())
                .withArguments('compileJava', 'jigReports')
                .withPluginClasspath()
                .build()

        then:
        result.task(":jigReports").outcome == SUCCESS
        testProjectDir.resolve(Path.of('build', 'jig', 'index.html')).toFile().exists()

        when:
        def cleanResult = GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(testProjectDir.toFile())
                .withArguments('clean')
                .withPluginClasspath()
                .build()
        then:
        cleanResult.task(":clean").outcome == SUCCESS
        !testProjectDir.resolve("build").toFile().exists()

        where:
        gradleVersion                   | buildScript
        GradleVersion.current().version | buildGradle
        GradleVersion.current().version | buildGradleKts
        '6.9.3'                         | buildGradle
        '6.9.3'                         | buildGradleKts
    }

    def writeSampleJavaSource() {
        def sourceDir = testProjectDir.resolve(Path.of('src', 'main', 'java', 'jig', 'domain'))
        Files.createDirectories(sourceDir)
        sourceDir.resolve("Sample.java") << """
        package jig.domain;
        
        public class Sample {
        }
        """
    }

    @Shared
    static def buildGradle = [
            name   : 'build.gradle',
            content: """
                plugins {
                    id 'java'
                    id 'org.dddjava.jig-gradle-plugin'
                }

                jig {
                    modelPattern = '.+\\\\.model\\\\..+'
                }
                """
    ]

    @Shared
    static def buildGradleKts = [
            name   : 'build.gradle.kts',
            content: """
                plugins {
                    java
                    id("org.dddjava.jig-gradle-plugin")
                }

                jig {
                    modelPattern = ".+\\\\.model\\\\..+"
                    outputOmitPrefix = ".+\\\\.model\\\\."
                    documentTypes = listOf("PackageRelationDiagram")
                }
                """
    ]
}
