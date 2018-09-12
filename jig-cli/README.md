# JIG: コマンドライン版

以下のドキュメントを出力するコマンドラインツールです。

## ダウンロード

[直近のリリース](https://github.com/dddjava/Jig/releases/latest)から `jig-cli.jar` をダウンロードしてください。

## 実行方法

```
java -jar jig-cli.jar
```

`./build/jig` ディレクトリにドキュメントが出力されます。
出力されるドキュメントは[JigDocument](../jig-core/src/main/java/org/dddjava/jig/presentation/view/JigDocument.java)を参照してください。

### プロパティ

次のように `--`に続けて指定します。

```
java -jar jig-cli.jar --jig.debug=true
```

設定できるプロパティは [application.propertis](./src/main/resources/application.properties) を参照してください。
