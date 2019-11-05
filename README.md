# JIG

[![CircleCI](https://circleci.com/gh/dddjava/Jig/tree/master.svg?style=svg)](https://circleci.com/gh/dddjava/Jig/tree/master)
[![Build status](https://ci.appveyor.com/api/projects/status/yklsnjlvds0l3ka5/branch/master?svg=true)](https://ci.appveyor.com/project/irof/jig/branch/master)

[JIGの紹介](https://speakerdeck.com/irof/jigfalseshao-jie)

## コンセプト

三層＋ドメインモデルのアーキテクチャで実装されたコードから、以下の分析・設計情報を生成します。

- ドメインモデルのクラスに記述された業務の概念とビジネスルール
- アプリケーション層に記述された業務機能

### 想定するアーキテクチャ

三層＋ドメインモデルのアーキテクチャでの使用を想定しています。

![ドメインモデルのクラスに記述された業務の概念とビジネスルール](./overview.png)

![アプリケーション層に記述された業務機能](./architecture.png)

## JIGドキュメント

JIGの生成する分析・設計情報をJIGドキュメントと呼びます。

種類は [JigDocument](./jig-core/src/main/java/org/dddjava/jig/domain/model/diagram/JigDocument.java) を参照してください。

## 使い方

JIGは二種類の実行方法を提供しています。使い方や設定はそれぞれのREADMEを参照してください。

- [コマンドライン](./jig-cli)
  - [Kotlin用](./jig-cli-kt)
- [Gradleプラグイン](./jig-gradle-plugin)

### 実行環境

- Java8以降。
- ダイアグラム出力には [Graphviz](https://www.graphviz.org/) が必要です。

## LICENSE

[Apache License 2.0](LICENSE)
