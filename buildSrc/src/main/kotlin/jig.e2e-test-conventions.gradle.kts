// 配布物を別プロセスで起動する E2E テスト。
// 実行が重いので check には載せず、qualityCheck から明示的に呼ぶ。
plugins {
    id("jig.java-conventions")
    `java-library`
}

val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

testing {
    suites {
        register<JvmTestSuite>("e2eTest") {
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

// カバレッジのレポートにe2eTestの実行分を含めるため、順序を固定する。
// 別プロセスで起動するCLI自体は計測されず、含まれるのはテスト側のJVMで動いた分だけ
tasks.named<JacocoReport>("jacocoTestReport") {
    mustRunAfter(tasks.named("e2eTest"))
}
