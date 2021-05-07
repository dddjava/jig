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

## 設定

`build.gradle` で指定できます。以下は例です。
```
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
