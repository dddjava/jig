下記のタスクをプロジェクトに組み込みます

* jigList: クラス一覧出力
* jigPackageDiagram: パッケージ関連図出力

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
        classpath 'org.dddjava.jig:jig-gradle-plugin:2018.4.4'
    }
}

apply plugin: 'jig-gradle-plugin'

jigList.dependsOn(compileJava)
jigPackageDiagram.dependsOn(compileJava)
```

## 設定(値はデフォルト)
```
jigListConfig {
    outputDirectory = 'build/reports' //出力ディレクトリ
    outputOmitPrefix= '.+\\.(service|domain\\.(model|basic))\\.' //出力時に省略する接頭辞パターン
}

jigPackageDiagramConfig {
    outputDirectory = 'build/reports' //出力ディレクトリ
    outputOmitPrefix= '.+\\.(service|domain\\.(model|basic))\\.' //出力時に省略する接頭辞パターン
    deps = -1 //出力する最大のパッケージ階層(-1は制限なし）
}
```

