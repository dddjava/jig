下記のタスクをプロジェクトに組み込みます

* jigList: クラス一覧出力
* jigPackageDiagram: パッケージ関連図出力

## 適用方法
現時点ではプラグインリポジトリに公開していないので、`./gradlew gradle-plugin:fatJar`でjarファイルを作り
任意のディレクトリに配置した上で、`build.gradle` に以下を記述

```
buildscript {
     dependencies {
        classpath files('{Jarファイルの配置ディレクトリ}/gradle-jig-plugin-all.jar')
     }
}

apply plugin: 'com.github.irof.Jig'
```

## 設定(値はデフォルト)
```
jigList {
    outputPath = 'build/reports/output.xlsx' //出力ディレクトリ
    outputOmitPrefix= '.+\\.(service|domain\\.(model|basic))\\.' //出力時に省略する接頭辞パターン
}

jigPackageDiagram {
    packagePattern = '.*.domain.model' //出力対象のパッケージ
    outputDiagramName = 'build/reports/output.png' //出力ファイル名
    outputOmitPrefix = '.+\.(service|domain\.(model|basic))\.' //省略するプレフィックス
    deps = -1 //出力する最大のパッケージ階層(-1は制限なし）
}
```

