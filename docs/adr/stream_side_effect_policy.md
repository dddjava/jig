# Stream処理で副作用を避ける

## 状況 (Context)

`SpringDataJdbcStatementsReader#readFrom` では、`Stream#forEach` の中で外部の `Map` に `put` する実装を行っていました。
この実装は次の問題を持ちます。

- Stream の中間処理と終端処理が副作用に依存し、意図が追いにくい
- 変換ロジックと格納ロジックが分離されず、読みやすさが下がる
- 並列化や再利用を検討しづらい構造になる

## 決定 (Decision)

Stream では副作用を避け、`map` / `flatMap` で値を組み立て、`collect` で集約する方針を採用します。

今回の対象では、`forEach` で `Map` に `put` する構成を廃止し、次の構成へ変更しました。

- `flatMap` で `SqlStatement` を生成する
- `collect(toMap(..., LinkedHashMap::new))` で `Map<SqlStatementId, SqlStatement>` を構築する

## 根拠・背景 (Rationale)

- **可読性の向上**  
  「どの値を作るか」と「どう集約するか」が明確になります。

- **保守性の向上**  
  外部状態の更新を減らし、処理の変更点を局所化できます。

- **実装の一貫性**  
  Stream の宣言的な使い方に合わせることで、同種のコードレビュー基準を統一できます。

## トレードオフ (Trade-Offs)

- `Optional` を扱うために `map` / `flatMap(Optional::stream)` が増え、初見では処理が長く見える場合があります。
- 重複キーの取り扱いを `toMap` のマージ関数で明示する必要があります。

## 採用ガイドライン・実装指針 (Guidelines)

- `forEach` で外部コレクションを更新する実装は原則避ける
- 生成は `map`、条件付き展開は `flatMap`、集約は `collect` に分離する
- `toMap` を使う場合は、重複時の振る舞いと `Map` 実装（順序要件があれば `LinkedHashMap`）を明示する

