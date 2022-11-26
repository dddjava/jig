# JIG-Gradle-Plugin

GradleプロジェクトでJIGドキュメントを出力するプラグインです。

## 導入方法

```build.gradle
plugins {
  id "org.dddjava.jig-gradle-plugin" version {バージョン}
}
```

バージョンや記述方法は [プラグインリポジトリ](https://plugins.gradle.org/plugin/org.dddjava.jig-gradle-plugin) を参照してください。

## タスク

`gradle tasks` で出力される "JIG tasks" を参照してください。

## Getting Started

1. プラグインの追加（導入方法参照）
1. プロジェクトのビルドおよびJIGの実行

### プロジェクトのビルドおよびJIGの実行

```
$ gradle clean build jig
```

`build/jig` ディレクトリにJIGドキュメントが出力されます。

## JIGの設定

`build.gradle` で指定できます。以下は例です。

```gradle
jig {
    // パッケージにかかわらず全ての要素を出力する
    modelPattern = '.+'
    // パッケージ関連図とビジネスルール一覧のみ出力する
    documentTypes = ['PackageRelationDiagram', 'BusinessRuleList']
    // 図のリンクのprefix: ダイアグラムのクラスなどからソースコードにリンクする（SVG限定）
    linkPrefix = "https://github.com/dddjava/jig/tree/master/jig-core/src/main/java"
}
```

設定できる項目は [JigConfig.java](./src/main/java/org/dddjava/jig/gradle/JigConfig.java) を参照してください。

### タスクの依存・前後設定
JIGは `*.class` が出力されていることを前提にしています。
そのため前述のように `clean` および `build` タスクを実行することで正しいドキュメントが得られます。
`clean` を実行しなければ以前に出力された `*.class` ファイルが残っていると不正なドキュメントになる可能性があります。
また、 `build` を実行しなければ「何も得られない」もしくは「コードを変えているのに何も変わらない」と言ったことが起こります。

そのため以下のように `jigReports` タスク実行時は必ず `clean` および `classes` が実行されるようにしておくと安定したJIGドキュメントが得られます。

```gradle
classes.mustRunAfter(clean)
jigReports.dependsOn(clean, classes)
```


## ログの見方
期待した出力がされない場合など、ログを確認する際は `--info` などを指定してください。
JIGの基本的なログは `INFO` で出力しています。
Gradleのデフォルトログレベルは [LIFECYCLE以上](https://docs.gradle.org/current/userguide/logging.html) のため、 `INFO` は表示されません。

## プラグイン開発者向け

### SNAPSHOTの適用

`mavenLocal()` へのインストールを行う

```
./gradlew build
```

`build.gradle` に以下を記述

```
buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath 'org.dddjava.jig:jig-gradle-plugin:+'
    }
}

apply plugin: 'java'
apply plugin: 'org.dddjava.jig-gradle-plugin'
```
