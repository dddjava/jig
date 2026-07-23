plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "jig"

include("jig-core")
include("jig-cli")
include("jig-gradle-plugin")
// テスト専用。maven-publish を適用しないため公開対象に入らない
include("jig-test-fixtures")
