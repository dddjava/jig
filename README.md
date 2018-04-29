# Jig
[![CircleCI](https://circleci.com/gh/dddjava/Jig.svg?style=shield&circle-token=2df75d7af763e76412fcd82077d80e99a9a95251)](https://circleci.com/gh/dddjava/Jig)

## コンセプト

三層＋ドメインモデルのアーキテクチャで実装されたコードから、以下の分析・設計情報を、生成する。

- ドメインモデルのクラスに記述された業務の概念とビジネスルール
- アプリケーション層に記述された業務機能

## 使い方

- [コマンドライン](./jig-cli)
- [Gradleプラグイン](./jig-gradle-plugin)

## ビルド

 `./gradlew clean build`

## リリース手順

- [GitHubでリリースを作成](https://github.com/dddjava/Jig/releases/new)
  - version: `{年}.{月}.{週}`
- CircleCIのArtifactsから `jig-cli.jar` を取得
- Edit releaseからアップロード

## LICENSE

[Apache License 2.0](LICENSE)

