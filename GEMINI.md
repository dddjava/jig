# JIG プロジェクト概要

JIGは、Javaのバイトコード（classファイル）を解析して、ソフトウェアの設計情報を可視化するためのツールです。ドメイン駆動設計（DDD）の概念を取り入れ、三層＋ドメインモデルのアーキテクチャで実装されたコードから、業務ルールや機能一覧、パッケージ関連図などを生成します。

## プロジェクト構成

プロジェクトは以下の3つの主要モジュールで構成されています。

- `jig-core`: 解析ロジックとドキュメント生成の本体。Maven Centralに公開されるコアライブラリ。
- `jig-cli`: `jig-core` をコマンドラインから実行するためのSpring Bootアプリケーション。
- `jig-gradle-plugin`: GradleプロジェクトにJIGを組み込むためのプラグイン。

## 技術スタック

| カテゴリ | 技術 |
| :--- | :--- |
| 言語 | Java 21+ (解析対象はJava 8以降) |
| バイトコード解析 | ASM |
| ソース解析 | JavaParser |
| HTMLテンプレート | Thymeleaf |
| Excel出力 | Apache POI |
| MyBatis解析 | MyBatis |
| CLI基盤 | Spring Boot |
| テスト (Java) | JUnit Jupiter 5, Mockito |
| テスト (JS) | Node.js (built-in test runner), jsdom |

## アーキテクチャ (jig-core)

`jig-core` はDDDスタイルのレイヤードアーキテクチャを採用しています。

- `org.dddjava.jig.domain.model`: ドメイン層。解析データ（data）、ドキュメント定義（documents）、分析済み情報（information）、ソース読み取り（sources）を含む。
- `org.dddjava.jig.application`: アプリケーション層。`JigService` など。
- `org.dddjava.jig.adapter`: アダプター層。HTML, Graphviz, Mermaid, Excel (POI), JSON などの出力。
- `org.dddjava.jig.infrastructure`: インフラストラクチャ層。ASM, JavaParser, MyBatis などの具体的な実装。

メインエントリポイントは `org.dddjava.jig.JigExecutor` です。

## ビルドとテスト

プロジェクトのビルドとテストには Gradle と Node.js (JSテスト用) を使用します。

### 主要なコマンド

- **フルテスト (Java + JS)**: `npm run test:full`
- **Javaテストのみ**: `./gradlew test`
- **JSテストのみ**: `npm run test`
- **特定サブプロジェクトのテスト**: `./gradlew :jig-core:test`
- **ビルド**: `./gradlew build`

JSテストは生成されたHTMLドキュメントの検証などに使用されており、`jig-core/src/test/js/` に配置されています。

## 開発コンベンション

### コミットメッセージ

Conventional Commits 形式を用い、**日本語**で記述します。

```
<type>[optional scope]: <説明>

<変更内容の要約>
```

- `type`: `feat`, `fix`, `refactor`, `docs`, `test`, `other`
- フッター（`--trailer`）:
    - `JIG-DOCUMENT: <documentName>` (例: `BusinessRuleList`)
    - `AGENT: <agentName>` (例: `Gemini`)

### ドキュメント定義

生成されるドキュメントの種類は `org.dddjava.jig.domain.model.documents.documentformat.JigDocument` で定義されています。新しいドキュメントを追加する場合は、このenumへの追加と、対応するテンプレートファイルの作成が必要です。
