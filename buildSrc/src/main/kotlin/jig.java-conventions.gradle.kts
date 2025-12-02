plugins {
    id("jig.base-conventions")
    `java-library`
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
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

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.12.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}