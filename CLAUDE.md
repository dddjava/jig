# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 概要

JIG はバイトコード（classファイル）からコードの設計を可視化するツール。パッケージ図・クラス図・HTML一覧などを生成する。Java 21 以降が必要（解析対象は Java 8 以上）。

## コマンド

### ビルド・テスト

```bash
# フルテスト（Java + JS）
npm run test:full

# Java テストのみ
./gradlew test

# JS テストのみ（*.js のみ変更した場合）
npm run test

# 特定サブプロジェクトのテスト
./gradlew :jig-core:test
./gradlew :jig-cli:test

# 単一テストクラスの実行
./gradlew :jig-core:test --tests "org.dddjava.jig.JigExecutorTest"

# 単一テストメソッドの実行
./gradlew :jig-core:test --tests "org.dddjava.jig.JigExecutorTest.methodName"

# 単一 JS テストファイル
node --test jig-core/src/test/js/insight.test.js
```

### テスト実行ポリシー（AGENTS.md より）

- `*.js` ファイルのみ変更 → `npm run test`
- それ以外（または混在） → `npm run test:full`
- CSS のみの変更、または `docs` コミットに該当する変更のみテスト省略可

## アーキテクチャ

### モジュール構成

- `jig-core/` — コアライブラリ（Maven Central 公開）。解析ロジックとドキュメント生成の本体
- `jig-cli/` — Spring Boot 実行可能 JAR。`jig-core` を CLI から実行する
- `jig-gradle-plugin/` — Gradle プラグイン（Gradle Plugin Portal 公開）

### jig-core パッケージ構造

DDDスタイルのレイヤード構成:

```
org.dddjava.jig/
├── domain/model/
│   ├── data/          # 生データ（types, packages, members 等）
│   ├── documents/     # ドキュメント定義（JigDocument enum を含む）
│   ├── information/   # 分析済み情報（applications, types 等）
│   └── sources/       # ソース読み取り（filesystem, javasources, mybatis 等）
├── application/       # JigService 等のアプリケーションサービス
├── adapter/           # 出力アダプター（html, graphviz, mermaid, poi, json 等）
└── infrastructure/    # インフラ実装（asm, javaparser, configuration 等）
```

メインエントリポイント: `JigExecutor.java`

### 技術スタック

| 用途 | 技術 |
|------|------|
| バイトコード解析 | ASM |
| Java ソース解析 | JavaParser |
| HTML テンプレート | Thymeleaf |
| Excel 出力 | Apache POI |
| MyBatis SQL 解析 | MyBatis |
| CLI 設定・起動 | Spring Boot |
| テスト | JUnit Jupiter 5 + Mockito |
| JS テスト | Node.js 組み込み test runner |

### 出力ドキュメント

`JigDocument.java` に enum として定義。主要なもの:
- `BusinessRuleList` — ビジネスルール一覧
- `ApplicationList` — 機能一覧
- `ListOutput` — 一覧出力（HTML）
- `Insight` — インサイト
- `Glossary` — 用語集

## コミットルール（AGENTS.md より）

Conventional Commits 形式で **日本語** で記述する。

```
<type>[optional scope]: <説明>

<変更内容の要約>
```

本文（body）には変更内容の要約を記述する。

使用可能な type: `feat`, `fix`, `refactor`, `docs`, `test`, `other`

フッター:
- `JIG-DOCUMENT: <documentName>` — 変更対象の JigDocument が特定できる場合
- `AGENT: <agentName>` — 自動エージェントがコミットする場合（例: `Claude`）

フッターは `--trailer` オプションで付与:
```bash
git commit -m "feat: ..." --trailer "JIG-DOCUMENT: Insight" --trailer "AGENT: Claude"
```

テンプレートファイル `templates/<name>.html` と `JigDocument` の enum 名の対応は `JigDocument.java` を参照。
