import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("jig.java-conventions")
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
}