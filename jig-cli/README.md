# JIG: コマンドライン版

以下のドキュメントを出力するコマンドラインツールです。

- ビジネスルール一覧（EXCEL）
- 入出力一覧（EXCEL）
- パッケージ依存図（PNG）
- サービスメソッド関連図（PNG）

## ダウンロード

[直近のリリース](https://github.com/dddjava/Jig/releases/latest)から `jig-cli.jar` をダウンロードしてください。

## 実行方法

```
java -jar jig-cli.jar
```

`./build/jig` ディレクトリに以下のファイルが出力されます。

- jig-list_application.xlsx
- jig-list_domain.xlsx
- jig-diagram_package-dependency.png
- jig-diagram_service-method-call-hierarchy.png

### プロパティ

次のように `--`に続けて指定します。

```
java -jar jig-cli.jar --depth=7　--outputDirectory=.
```

上記ではパッケージ依存図の出力が7階層になり、ドキュメントはカレントディレクトリに出力されます。

```
# 出力対象（カンマで複数指定）
documentType=ServiceMethodCallHierarchy,PackageDependency,ClassList
# パッケージ依存図で出力する階層（-1は無制限）
depth=-1
# 出力ディレクトリ
outputDirectory=./
```

その他のプロパティは [application.propertis](./src/main/resources/application.properties) を参照してください。

## クラス情報一覧

以下の一覧を出力します。

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
- 条件分岐箇所の一覧

### 出力項目

出力項目は以下を参照してください。

- 機能情報の出力項目
    - [サービス](../jig-core/src/main/java/jig/domain/model/report/ServiceReport.java)
    - [データソース](../jig-core/src/main/java/jig/domain/model/report/DatasourceReport.java)
- モデル情報の出力項目
    - [列挙子](../jig-core/src/main/java/jig/domain/model/report/EnumReport.java)
    - [その他](../jig-core/src/main/java/jig/domain/model/report/GenericModelReport.java)
- [バリデーション情報の出力項目](../jig-core/src/main/java/jig/domain/model/report/ValidationReport.java)
- [文字列比較箇所の出力項目](../jig-core/src/main/java/jig/domain/model/report/StringComparingReport.java)
- [条件分岐箇所の出力項目](../jig-core/src/main/java/jig/domain/model/report/DecisionReport.java)

#### 補足: データソースのテーブル名

MyBatisを使用していることによる制限があります。[詳細はJavadocを参照してください。](../jig-core/src/main/java/org/dddjava/jig/infrastructure/mybatis/MyBatisSqlReader.java#L23-L27)

#### 補足: バリデーション情報

- `javax.validation` および `org.hibernate.validator` パッケージのアノテーションを対象にしています。
    - 他のアノテーションは対応していません。
- アノテーション記述はシンプルなもののみ出力しています。 [#75](https://github.com/irof/Jig/issues/75)
    - 配列およびアノテーションで指定するものは `[...]` で出力しています。
- フィールドとメソッドの指定されたアノテーションのみを対象にしています。

## パッケージ依存図

パッケージ間の依存図をPNGで出力します。
出力対象のパッケージは「モデルと判断されるもの」です。
