# Jig
[![CircleCI](https://circleci.com/gh/irof/Jig.svg?style=shield&circle-token=2df75d7af763e76412fcd82077d80e99a9a95251)](https://circleci.com/gh/irof/Jig)

## 使い方

- コマンドライン
  - [一覧出力](./class-list-cli)
  - [ダイアグラム出力](./package-diagram-cli)
- [Gradleプラグイン](./gradle-plugin)

## 必要な環境

- GraphViz
  - dotを使ってるので http://plantuml.com/graphviz-dot

### Windows

graphviz-2.38.msi で動作確認しています。

https://graphviz.gitlab.io/_pages/Download/Download_windows.html

## ビルド

```
./gradlew clean build
```

