// 代表プロジェクトを解析対象のクラスファイルとして提供するテスト専用モジュール。
// JIGの入力はクラスファイルなので、Javaソースをリソースとして置くだけでは解析対象にならない。
// 代表プロジェクトごとに専用のコンパイルタスクを登録し、build/fixtures 配下へ出力する。
plugins {
    id("jig.java-conventions")
}

val fixturesRoot = layout.buildDirectory.dir("fixtures")

/**
 * 代表プロジェクトのコンパイルとソース配置を登録する。
 *
 * @param name projects/ 配下のディレクトリ名
 * @param releases 生成するクラスファイルのバージョン。解析対象バージョンの契約を持つfixtureだけ複数指定する
 */
fun registerFixtureProject(name: String, releases: List<Int>): List<TaskProvider<*>> {
    val projectSource = layout.projectDirectory.dir("projects/$name/src/main/java")

    val compileTasks = releases.map { release ->
        tasks.register<JavaCompile>("compileFixture-$name-$release") {
            description = "$name を Java $release のクラスファイルへコンパイルする"
            // 下限のreleaseを指定できるのが最も新しいtoolchainなので、そこから各バージョンを作る
            javaCompiler = javaToolchains.compilerFor {
                languageVersion = JavaLanguageVersion.of(25)
            }
            source = fileTree(projectSource)
            destinationDirectory = fixturesRoot.map { it.dir("$name/classes-$release") }
            options.release = release
            // 代表プロジェクトは互いに独立させ、JIG本体にも依存させない
            classpath = files()
        }
    }

    // JIGはJavadocをソースから読むため、クラスファイルと同じ場所にソースも置く
    val syncSources = tasks.register<Sync>("syncFixtureSources-$name") {
        from(projectSource)
        into(fixturesRoot.map { it.dir("$name/sources") })
    }

    // 生成したバージョンを宣言する。ディレクトリの有無で判定すると、
    // 生成対象を減らしたときに以前の出力が残って利用側が誤認する
    val writeReleases = tasks.register("writeFixtureReleases-$name") {
        val releasesFile = fixturesRoot.map { it.file("$name/releases.txt") }
        inputs.property("releases", releases)
        outputs.file(releasesFile)
        doLast {
            releasesFile.get().asFile.writeText(releases.joinToString("\n"))
        }
    }

    return compileTasks + syncSources + writeReleases
}

// CircleCIはJDK21のイメージで動くため、最新LTSのクラスファイル生成はtoolchainの自動取得を伴う。
// 同じ検証をGitHub Actions側（PR gate・main upkeep・release）で行っているので、ここでは作らない。
val latestLtsRelease = if (System.getenv("CIRCLECI") == "true") emptyList() else listOf(25)

val fixtureTasks = listOf(
    registerFixtureProject("minimal-java", listOf(21)),
    registerFixtureProject("showcase", listOf(21)),
    registerFixtureProject("bytecode-compat", listOf(8, 21) + latestLtsRelease),
).flatten()

val fixtures = tasks.register("fixtures") {
    group = "build"
    description = "全ての代表プロジェクトをコンパイルして build/fixtures へ配置する"
    dependsOn(fixtureTasks)
}

// fixture を利用する側は JigFixtures API 経由でパスを得る。API 単体では意味がないため常に配置も行う
tasks.named("assemble") {
    dependsOn(fixtures)
}
