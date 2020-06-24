# JIG

[![CircleCI](https://circleci.com/gh/dddjava/jig/tree/master.svg?style=svg)](https://circleci.com/gh/dddjava/jig)
[![Build status](https://ci.appveyor.com/api/projects/status/yklsnjlvds0l3ka5/branch/master?svg=true)](https://ci.appveyor.com/project/irof/jig/branch/master)

## 紹介

JIGはバイトコードおよびソースコードから、一覧（Excel形式）やダイアグラム（SVG形式）を出力するツールです。実行には Java8以降 と [Graphviz](https://www.graphviz.org/) が必要です。

- [JIGの紹介](https://speakerdeck.com/irof/jigfalseshao-jie) （スライド）

## 使い方
- [コマンドラインでの使い方](./jig-cli)
  - [Kotlin用](./jig-cli-kt)
- [Gradleプラグインでの使い方](./jig-gradle-plugin)
- [チュートリアル](https://github.com/dddjava/jig-tutorial)

うまく動かない場合などは [issue](https://github.com/dddjava/jig/issues/new/choose) でお問い合わせください。

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

種類は [JigDocument](./jig-core/src/main/java/org/dddjava/jig/domain/model/jigdocument/JigDocument.java) を参照してください。

## LICENSE

[Apache License 2.0](LICENSE)
