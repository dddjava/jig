plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "jig"

include("jig-core")
include("jig-cli")
include("jig-gradle-plugin")
