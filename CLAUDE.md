# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 概要

JIG はバイトコード（classファイル）からコードの設計を可視化するツール。パッケージ図・クラス図・HTML一覧などを生成する。Java 21 以降が必要（解析対象は Java 8 以上）。

## コマンド

### ビルド・テスト

```bash
# 開発中の標準入口（JS + Java の Unit/Component/Contract）
npm run test:full

# コミット前・PR 前（CSS lint + JS + clean build qualityCheck）
npm run test:pr

# Java テストのみ
./gradlew test

# 公開形式の Contract のみ
./gradlew contractTest

# check + E2E + カバレッジレポート（PR CI と同じ Java 側入口）
./gradlew qualityCheck

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

# CSS lint
npm run lint:css
```

### テスト実行ポリシー

- `*.js` ファイルのみ変更 → `npm run test`
- それ以外（または混在） → `npm run test:full`
- コミット前・PR 前 → `npm run test:pr`（PR CI と同じ組合せ）
- CSS のみの変更、または `docs` コミットに該当する変更のみテスト省略可

テストの階層とタスクの対応は `docs/test-architecture.md` を参照。`check` は Unit/Component/Contract まで、E2E は `qualityCheck` から実行する。

### ブラウザでの見た目確認（Playwright）

HTML/Mermaid図の見た目確認手順は `jig-core/src/test/playwright/README.md` を参照。JIGは自分自身を解析対象にでき、`java -jar jig-cli/build/libs/jig-cli.jar` をリポジトリルートで実行すると `./build/jig/` にサンプルドキュメント一式が生成される。

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
│   ├── knowledge/     # 知識モデル（insight, usecases, module, smell 等）
│   └── sources/       # ソース読み取り（filesystem, javasources, mybatis 等）
├── annotation/        # アノテーション定義
├── application/       # JigService 等のアプリケーションサービス
├── adapter/           # 出力アダプター（datajs, json 等）
└── infrastructure/    # インフラ実装（asm, javaparser, configuration 等）
```

メインエントリポイント: `JigExecutor.java`

### 技術スタック

| 用途 | 技術 |
|------|------|
| バイトコード解析 | ASM |
| Java ソース解析 | JavaParser |
| MyBatis SQL 解析 | MyBatis |
| HTML 出力 | 静的テンプレート + JSON データ + クライアントサイド JS |
| 図の描画 | Mermaid（クライアントサイドで描画） |
| CLI 設定・起動 | Spring Boot |
| テスト | JUnit Jupiter 6 + Mockito |
| JS テスト | Node.js 組み込み test runner + jsdom |

### 出力ドキュメント

`JigDocument.java` に enum として定義。アクティブなもの:
- `Glossary` — 用語集
- `PackageRelation` — パッケージ関連
- `DomainModel` — ドメインモデル
- `Usecase` — ユースケース
- `InboundInterface` — 入力インタフェース
- `OutboundInterface` — 出力インタフェース
- `Insight` — インサイト
- `ListOutput` — 一覧出力（HTML）
- `LibraryDependency` — ライブラリ依存情報

## コミットルール

Conventional Commits 形式で **日本語** で記述する。

```
<type>[optional scope]: <説明>

<変更内容の要約>
```

本文（body）には変更内容の要約を記述する。

使用可能な type: `feat`, `fix`, `refactor`, `docs`, `test`, `other`

フッターは該当する場合のみ `--trailer` オプションで付与する。該当しないフッターは付けない（空値のトレーラーを作らない）。

- 変更対象の JigDocument が特定できる場合のみ: `--trailer "JIG-DOCUMENT: <documentName>"`（例: `Insight`）
- 自動エージェントがコミットする場合のみ: `--trailer "AGENT: <agentName>"`（例: `Claude`）
- issue を解消する場合のみ: `--trailer "Closes: #<番号>"`（`gh issue close` で直接クローズしない。close専用の別コミット・空コミットも作らない）

```bash
# 該当するフッターだけ付与する
git commit -m "feat: ..." --trailer "JIG-DOCUMENT: Insight" --trailer "AGENT: Claude"
git commit -m "fix: ..." --trailer "AGENT: Claude" --trailer "Closes: #1134"
```

テンプレートファイル `templates/<name>.html` と `JigDocument` の enum 名の対応は `JigDocument.java` を参照。
