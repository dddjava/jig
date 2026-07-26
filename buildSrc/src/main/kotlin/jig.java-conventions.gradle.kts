plugins {
    id("jig.base-conventions")
    `java-library`
    jacoco
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

val defaultEncoding = "UTF-8"
tasks.withType<JavaCompile>().configureEach {
    options.encoding = defaultEncoding
    options.compilerArgs.add("-Xlint:deprecation")
}
tasks.withType<Javadoc>().configureEach {
    options.encoding = defaultEncoding
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

tasks.named<Jar>("jar") {
    into("META-INF") {
        from(rootDir) {
            include("LICENSE")
        }
    }

    manifest {
        attributes["Implementation-Version"] = project.version.toString()
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

// カバレッジは監視のために収集する。しきい値による合否判定は置かない
tasks.named<JacocoReport>("jacocoTestReport") {
    // 既定はtestタスクの分だけを読む。contractTest/e2eTestの実行分も監視対象に含める。
    // 実行していないスイートの分を要求しないよう、存在するexecだけを拾うfileTreeで渡す
    executionData(objects.fileTree().from(layout.buildDirectory.dir("jacoco")).matching { include("*.exec") })

    reports {
        xml.required = true
        html.required = true
    }
}

val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    testImplementation(platform(libs.findLibrary("junit-bom").get()))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}