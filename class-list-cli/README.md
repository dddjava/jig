# class-list-cli

クラスの情報を出力します。

## 出力される情報

- 機能情報の一覧
    - サービス
    - データソース
- モデル情報の一覧
    - 識別子
    - 数値
    - 列挙子
    - 日付
    - 期間
    - ファーストクラスコレクション
- バリデーション情報の一覧
- 文字列比較箇所の一覧

### 出力項目

出力項目は以下を参照してください。

- [機能情報の出力項目](../jig-core/src/main/java/jig/domain/model/report/MethodPerspective.java)
- [モデル情報の出力項目](../jig-core/src/main/java/jig/domain/model/report/TypePerspective.java)
- [バリデーション情報の出力項目](../jig-core/src/main/java/jig/domain/model/report/TypePerspective.java)

#### 補足: データソースのテーブル名

データソースのCRUDに出力されるテーブル名の取得には、MyBatisの内部APIを使用しています。
SQLを実際に発行しているわけではないため、以下の制限があります。

- 動的SQLの組み立ては確実ではありません。
    - XMLでSQLを組み立てるもの（ifなどを含むもの）が該当します。
    - DEBUGログ: `動的SQLの組み立てをエミュレートしました。`
    - 対策: 実際の実装および動作を確認してください。
- `Mapper` インタフェースが未知のクラスを使用しているとテーブルが出力されません。
    - クラスパスに存在しないクラスを使用している場合などが該当します。
    - WARNログ: `Mapperが未知のクラスに依存しているため読み取れませんでした。`
    - 対策: 実行時のクラスパスに該当のクラスを含めてください。

#### 補足: バリデーション情報

- `javax.validation` および `org.hibernate.validator` パッケージのアノテーションを対象にしています。
    - 他のアノテーションは対応していません。
- アノテーション記述はシンプルなもののみ出力しています。 [#75](https://github.com/irof/Jig/issues/75)
    - 配列およびアノテーションで指定するものは `[...]` で出力しています。
- フィールドとメソッドの指定されたアノテーションのみを対象にしています。

## 実行

```
java -jar class-list-cli.jar 
```

### プロパティ

```
output.list.name=output.tsv
project.path=./
```

出力形式はtsvとxlsx。

GradleのJavaプロジェクトに対応。
`project.path` 配下の `src/main/java`, `build/classes/java/main`, `build/resources/main` ディレクトリから情報を取得します。
デフォルトでは `java` コマンドを実行したディレクトリになります。
