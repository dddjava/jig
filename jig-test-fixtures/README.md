# jig-test-fixtures

JIG の入力となる代表プロジェクトを提供するテスト専用モジュール。公開成果物には含めない。

設計の位置づけは `docs/adr/test_architecture_policy.md` を参照。

## 代表プロジェクトを追加する

1. `projects/<name>/src/main/java/` にソースを置き、`projects/<name>/README.md` に固定する契約と必要な出力を書く。
2. `build.gradle.kts` の `registerFixtureProject` に名前と生成するクラスファイルのバージョンを追加する。

JIG の入力はクラスファイルなので、ソースをリソースとして置くだけでは解析対象にならない。`registerFixtureProject` が代表プロジェクトごとに `JavaCompile` タスクを登録し、`build/fixtures/<name>/` へ次の形で配置する。

```
build/fixtures/<name>/classes-<release>/   # --release ごとのクラスファイル
build/fixtures/<name>/sources/             # ソース（JIGがJavadocを読む）
build/fixtures/<name>/releases.txt         # 生成したバージョンの宣言
```

生成するバージョンはビルド環境で変わる（CircleCI は JDK 21 のイメージのため最新 LTS を作らない）。利用側は `availableReleases()` で `releases.txt` から得る。ディレクトリの有無で判定すると、生成対象を減らしたときに以前の出力が残って誤認する。

代表プロジェクトは互いに独立させ、JIG 本体にも依存させない。ある代表プロジェクトの解析結果が他の内容に影響されないようにするため。

## 利用する

パスを組み立てず `JigFixtures` から受け取る。

```java
FixtureProject project = JigFixtures.project("bytecode-compat");
SourceBasePaths sourceBasePaths = new SourceBasePaths(
        new SourceBasePath(List.of(project.classes(21))),
        new SourceBasePath(List.of(project.sources())));
```

利用するテストタスクには、配置先とタスク依存の宣言が要る。

```groovy
tasks.named('contractTest') {
    dependsOn ':jig-test-fixtures:fixtures'
    systemProperty 'jig.fixtures.root', fixturesRoot.get().asFile.absolutePath
}
```
