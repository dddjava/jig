# `Optional` の使用方針

## 状況 (Context)

本プロジェクトでは [防御的プログラミングを行わない](./defensive_programming_policy.md) 方針のもと、`null` を許容しない設計を採用しています。
一方で「値が存在しないことがある」というドメイン上の事実を表現する必要がある場面は確実に存在し、その表現に `Optional` を使用しています。

ただし `Optional` は使い方を誤ると以下の問題を引き起こします。

- 戻り値・引数・フィールドのいずれでも無差別に使うと、API の読み手が「不在の意味」を都度推測する必要が生じる
- `Optional.isPresent()` + `get()` のような分岐主体の利用は、`null` チェックと同じ複雑さを再導入する
- `Optional` の入れ子（`Optional<Optional<T>>`、`Optional<List<T>>` 等）は意図不明瞭になりやすい

このため、`Optional` の使用場面と使い方の方針を定めます。

## 決定 (Decision)

1. **戻り値で「値が無いこと」がドメイン上意味を持つ場合に `Optional` を使う**
   - `find` / `resolve` / `lookup` など、検索結果として「該当なし」が正常系の一部となる API で使用する。
   - 例: `JigTypes#resolveJigType`, `JigTypes#resolveJigMethod`, `JigTypeMembers#findFieldByName`。

2. **引数・record コンポーネントでは「明示的に未指定であり得る」設定値・入力に限り使う**
   - 設定や外部入力の「省略可能性」を型で表明したい場合に限り、引数や record コンポーネントとして `Optional` を許容する。
   - 例: `JigSettings#domainPattern`（設定で省略され得る）、`DefaultJigRepositoryFactory#createJigRepository(..., Optional<Path> repositoryRoot)`。
   - 通常のドメインモデルの内部状態としては `Optional` 型のフィールドを使わず、未指定を別の手段（空コレクション、専用のドメイン型、NullObject 等）で表現する。

3. **コレクションを `Optional` で包まない**
   - 「空であること」と「不在であること」を区別する設計上の必要が無い限り、`Optional<List<T>>` や `Optional<Set<T>>` は使わず、空コレクションを返す。

4. **`Optional` は宣言的に扱う**
   - `isPresent()` / `get()` で分岐する代わりに、`map` / `flatMap` / `filter` / `orElse` / `ifPresent` / `stream()` を用いる。
   - `Optional::stream` と `flatMap` を組み合わせて Stream パイプラインに合流させてよい。

## 根拠・背景 (Rationale)

- **`null` 不使用方針との整合**  
  [防御的プログラミングを行わない](./defensive_programming_policy.md) と整合させるため、「値が無い可能性」は型で表現する。これにより呼び出し元での `null` チェックが不要になる。

- **意図の明示**  
  戻り値の `Optional` は「呼び出し元が不在ケースを必ず扱う必要がある」というシグナルとして機能する。引数・record の `Optional` は「ここは省略可能な入力である」という設計意図を伝える。

- **宣言的な合成**  
  `map` / `flatMap` 中心の利用により、[Stream 処理で副作用を避ける](./stream_side_effect_policy.md) と同様の宣言的なスタイルを保てる。

## トレードオフ (Trade-Offs)

- `Optional` を含む型シグネチャは読み手に一段の理解コストを課す。特に record コンポーネントでの `Optional` は Java 標準では推奨されない用法のため、本プロジェクト固有の判断として明示する必要がある。
- 不在を表すためのドメイン型（空コレクション、NullObject 等）と `Optional` のどちらを採るかは設計判断が必要で、機械的には決まらない。

## 採用ガイドライン (Guidelines)

1. **戻り値での利用**
   - 検索系メソッドで「該当なし」が正常系に含まれる場合に使用する。
   - 例外的状況を表すために `Optional.empty()` を返さない（その場合は例外を投げるか、ドメインの状態として表現する）。

2. **引数での利用**
   - 引数を `Optional` にするのは、その API が「省略可能な構成情報」を受け取ることを表明したい場合に限る。
   - 通常のメソッド引数の「省略可能性」はオーバーロードで表現することを優先する。

3. **record コンポーネントでの利用**
   - 設定オブジェクトなど「ユーザが明示的に値を与えなかったこと」を伝搬する必要がある型に限り使用する。
   - ドメイン上の値の不在を表したいだけならば、専用のドメイン型・空コレクション・NullObject を優先する。

4. **扱い方**
   - `isPresent()` + `get()` の組み合わせは避け、`map` / `flatMap` / `orElse` / `ifPresent` / `stream()` で合成する。
   - `Optional.of` と `Optional.ofNullable` を取り違えない。`null` を受け得る値は `ofNullable`、それ以外は `of`。
   - `Optional<List<T>>` / `Optional<Set<T>>` / `Optional<Optional<T>>` は作らない。

## 実装例

### 戻り値での利用（推奨）

```java
public Optional<JigType> resolveJigType(TypeId typeId) {
    return Optional.ofNullable(jigTypeMap.get(typeId));
}

// 呼び出し側は分岐ではなく合成で扱う
public boolean isService(JigMethodId jigMethodId) {
    return resolveJigMethod(jigMethodId)
            .flatMap(jigMethod -> resolveJigType(jigMethod.declaringType()))
            .filter(JigType::isService)
            .isPresent();
}
```

### 設定値での利用（推奨）

```java
public record JigSettings(
        Path outputDirectory,
        Optional<String> domainPattern, // ユーザが未指定であり得ることを型で表明
        List<JigDocument> jigDocuments,
        Locale locale
) implements JigDocumentContext { }
```

### 避ける例

```java
// 空コレクションで十分なケースで Optional を被せる
Optional<List<Item>> findItems(); // → List<Item> findItems() を返し、不在は空リストで表現する

// isPresent + get の分岐
if (opt.isPresent()) {
    use(opt.get());
} else {
    fallback();
}
// → opt.ifPresentOrElse(this::use, this::fallback);
// あるいは use(opt.orElseGet(this::fallback));
```

## 結論 (Consequences)

- 「値が無い可能性」が型で表現され、`null` チェックを書かない方針と整合する。
- 戻り値・引数・record コンポーネントでの使い分けが明確になり、API の意図が読み手に伝わる。
- 宣言的な扱いを徹底することで、分岐主体の `null` チェックと同型の複雑さを再導入することを防げる。
