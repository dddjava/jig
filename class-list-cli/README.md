# class-list-cli

クラスの情報を出力します。

## 実行

```
java -jar class-list-cli.jar 
```

## プロパティ

```
output.list.name=output.tsv
project.path=./sut
```

出力形式はtsvとxlsx。

GradleのJavaプロジェクトに対応。
`project.path` 配下の `src/main/java`, `build/classes/java/main`, `build/resources/main` ディレクトリから情報を取得します。

## 制限事項

REPOSITORY のCRUDテーブルは、MyBatisの `Mapper` インタフェースを利用しているため、以下の制限があります。

- 動的SQLの場合は正確ではありません。
  - MyBatisが `DynamicSqlSource` と判断するものが該当します。
  - DEBUGログ: `動的SQLの組み立てをエミュレートしました。`
- `Mapper` インタフェースが未知のクラスを使用していた場合はテーブルが出力されません。
  - クラスパスに存在しないクラスを使用している場合などが該当します。
  - WARNログ: `Mapperが未知のクラスに依存しているため読み取れませんでした。`

