下記のタスクをプロジェクトに組み込みます

* jigReports: レポート一式を出力

## プラグインリポジトリから取得

https://plugins.gradle.org/plugin/org.dddjava.jig-gradle-plugin を参照してください。

## SNAPSHOTの適用

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

jigReports.dependsOn(compileJava)
```

## 設定(値はデフォルト)
```
jig {
    documentTypes = ['ServiceMethodCallHierarchy','PackageDependency','ApplicationList','DomainList','BranchList','EnumUsage','BooleanService']
    outputDirectory = 'build/jig' //出力ディレクトリ
    outputOmitPrefix= '.+\\.(service|domain\\.(model|basic))\\.' //出力時に省略する接頭辞パターン
    depth = -1 //出力する最大のパッケージ階層(-1は制限なし）
}
```

