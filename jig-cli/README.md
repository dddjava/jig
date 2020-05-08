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

次のように `--`に続けて指定します。

```
$ java -jar jig-cli.jar --documentType=ServiceMethodCallHierarchyDiagram
```

設定できるプロパティは [application.properties](./src/main/resources/application.properties) を参照してください。

## 困ったら

[HELP](https://github.com/dddjava/jig/wiki/HELP) を参照してみてください。
