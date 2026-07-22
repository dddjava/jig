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
$ gradle jigReports
```

`build/jig` ディレクトリにJIGドキュメントが出力されます。
`jigReports` はコンパイル（`classes`）に依存しているため、コンパイルは自動的に先行して実行されます。

## JIGの設定

`build.gradle` で指定できます。以下は例です。

```gradle
jig {
    // パッケージにかかわらず全ての要素を出力する
    modelPattern = '.+'
    // パッケージ概要と一覧出力のみ出力する
    documentTypes = ['PackageRelation', 'ListOutput']
}
```

設定できる項目は [JigConfig.java](./src/main/java/org/dddjava/jig/gradle/JigConfig.java) を参照してください。

### タスクの依存・前後設定
JIGは `*.class` および Java ソースを入力とします。
これらはタスクの入力として宣言されており、コンパイルタスクへの依存も張られています。
そのため `jigReports` を単体で実行すればコンパイルが先行し、入力に変更がなければ `UP-TO-DATE` としてスキップされます。

なお、削除したクラスの `*.class` がビルドディレクトリに残っていると、それが解析対象に含まれてしまう可能性があります。
確実にクリーンな状態から出力したい場合は `clean` を併用してください。

```
$ gradle clean jigReports
```

`clean` との実行順序はGradleが保証しないため、常にこの順序としたい場合は以下を記述します。

```gradle
jigReports.mustRunAfter(clean)
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
