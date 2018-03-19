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
