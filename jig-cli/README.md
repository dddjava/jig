# JIG: コマンドライン版

以下のドキュメントを出力するコマンドラインツールです。


## Getting Started

Gradleを使用したシンプルなJavaプロジェクトを想定した手順です。
Mavenなど他のビルドツールを使用していたり、ソースやターゲットディレクトリを変更している場合などは設定が必要になります。

1. JIGのダウンロード
1. プロジェクトのビルド
1. JIGの実行

### JIGのダウンロード

[直近のリリース](https://github.com/dddjava/jig/releases/latest)から `jig-cli.jar` をダウンロードしてください。

### プロジェクトのビルド

対象プロジェクトをビルドします。

```
$ cd {対象プロジェクトのディレクトリ}
$ gradle clean build
```

### JIGの実行

プロジェクトのルートディレクトリで実行します。

```
$ java -jar {JIGをダウンロードしたディレクトリ}/jig-cli.jar
```

`./build/jig` ディレクトリにJIGドキュメントが出力されます。
コードを変更したら、再度プロジェクトのビルドから実行してください。

## 設定

JIG 自身の設定（`jig.*`）は以下の優先順位で解決されます。

1. CLI 引数（`--jig.document.types=PackageRelation,ListOutput` のように指定）
2. `{user.dir}/jig.properties`
3. `{user.home}/.jig/jig.properties`
4. コード内デフォルト

Spring Boot 由来の設定（ログレベルや読み込みディレクトリ）は [application.yml](./src/main/resources/application.yml) を参照してください。

## 困ったら

[HELP](https://github.com/dddjava/jig/wiki/HELP) を参照してみてください。
