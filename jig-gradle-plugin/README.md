下記のタスクをプロジェクトに組み込みます

* jigReports: レポート一式を出力

## 適用方法
現時点ではプラグインリポジトリに公開していないので `mavenLocal()` へのインストールを行う
```
./gradlew jig-core:publishToMevenLocal
./gradlew jig-gradle-plugin:publishToMevenLocal
```

`build.gradle` に以下を記述

```
buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath 'org.dddjava.jig:jig-gradle-plugin:2018.5.1'
    }
}

apply plugin: 'java'
apply plugin: 'jig-gradle-plugin'

jigReports.dependsOn(compileJava)
```

## 設定(値はデフォルト)
```
jig {
    documentTypes = ['ServiceMethodCallHierarchy','PackageDependency','ClassList']
    outputDirectory = 'build/reports' //出力ディレクトリ
    outputOmitPrefix= '.+\\.(service|domain\\.(model|basic))\\.' //出力時に省略する接頭辞パターン
    deps = -1 //出力する最大のパッケージ階層(-1は制限なし）
}
```

