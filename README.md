# Jig

## コンセプト

三層＋ドメインモデルのアーキテクチャで実装されたコードから、以下の分析・設計情報を、生成する。

- ドメインモデルのクラスに記述された業務の概念とビジネスルール
- アプリケーション層に記述された業務機能

### アーキテクチャ概要

![ドメインモデルのクラスに記述された業務の概念とビジネスルール](./overview.png)

### 各レイヤーのクラスの例

![アプリケーション層に記述された業務機能](./architecture.png)

## 使い方

- [コマンドライン](./jig-cli)
- [Gradleプラグイン](./jig-gradle-plugin)

## ビルド

 `./gradlew clean build`

## リリース手順

- [GitHubでリリースページを作成](https://github.com/dddjava/Jig/releases/new)
  - version: `{年}.{月}.{週}`
- [CircleCI](https://circleci.com/gh/dddjava/Jig)のリリースビルドから `jig-cli.jar` を取得
- リリースページのEdit releaseからjarをアップロード

## LICENSE

[Apache License 2.0](LICENSE)

