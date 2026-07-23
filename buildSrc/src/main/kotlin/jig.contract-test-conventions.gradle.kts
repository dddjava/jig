// JIGが生成・消費する公開形式（JSON・データJS・HTML）の Contract テスト。
// 軽いので check に載せ、build から実行されるようにする。
plugins {
    id("jig.java-conventions")
    `java-library`
}

val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

testing {
    suites {
        register<JvmTestSuite>("contractTest") {
            dependencies {
                implementation(project())
                implementation(platform(libs.findLibrary("junit-bom").get()))
                implementation("org.junit.jupiter:junit-jupiter")
                runtimeOnly("org.junit.platform:junit-platform-launcher")
            }
            targets.all {
                testTask.configure {
                    useJUnitPlatform()
                    shouldRunAfter(tasks.named("test"))
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("contractTest"))
}
