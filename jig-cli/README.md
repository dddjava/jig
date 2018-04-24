# JIG: コマンドライン版

以下のドキュメントを出力するコマンドラインツールです。

- クラス情報一覧（EXCEL）
- パッケージ依存図（PNG）
- サービスメソッド関連図（PNG）

## 実行方法

```
java -jar jig-cli.jar
```

カレントディレクトリに以下のファイルが出力されます。

- jig-report-class-list.xlsx
- jig-diagram_package-dependency.png
- jig-diagram_service-method-call-hierarchy.png

### プロパティ

次のように `--`に続けて指定します。

```
java -jar jig-cli.jar --depth=7　--outputDirectory=.jig documentType=PackageDependency,ClassList
```

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

## パッケージ依存図

パッケージ間の依存図をPNGで出力します。
出力対象のパッケージは

### PlantUMLで出力する場合

- ビルド時にPlantUMLを含めるように指定する
- 実行環境にGraphVizをインストールする
- 実行時にプロパティを指定する

#### ビルド

```
./gradlew clean build -PincludePlantUML
```

`plantuml.jar` が同梱された `jig-diagram-cli.jar` が作成されます。

#### 実行環境

GraphVizのインストールが必要になります。
Windowsは graphviz-2.38.msi で動作確認しています。

- http://plantuml.com/graphviz-dot
- https://graphviz.gitlab.io/_pages/Download/Download_windows.html

#### 実行方法

`jig.diagram.writer=plantuml` を指定してください。コマンドラインで実行する場合は次のようになります。

```
java -jar package-diagram-cli.jar --jig.diagram.writer=plantuml
```
