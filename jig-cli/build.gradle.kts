import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("jig.java-conventions")
    id("jig.e2e-test-conventions")
    id("org.springframework.boot")
}

tasks.named<BootJar>("bootJar") {
    into("META-INF") {
        from(rootDir) {
            include("LICENSE")
        }
    }
    archiveFileName.set("jig-cli.jar")
}

tasks.named<Jar>("jar") {
    // *-plain.jarを作らない
    enabled = false
}

dependencies {
    implementation(project(":jig-core"))

    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    e2eTestImplementation(project(":jig-test-fixtures"))
}

// E2Eは配布物を別プロセスで起動するため、jarのパスと代表プロジェクトの配置先を渡す
val bootJarTask = tasks.named<BootJar>("bootJar")
tasks.named<Test>("e2eTest") {
    dependsOn(bootJarTask, ":jig-test-fixtures:fixtures")
    systemProperty("jig.cli.jar", bootJarTask.get().archiveFile.get().asFile.absolutePath)
    systemProperty(
        "jig.fixtures.root",
        project(":jig-test-fixtures").layout.buildDirectory.dir("fixtures").get().asFile.absolutePath
    )
}