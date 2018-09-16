# JIG: コマンドライン版（SpringBoot未使用）

以下のドキュメントを出力するコマンドラインツールのSpringBootを使用しない実験版です。

## ダウンロード

[直近のリリース](https://github.com/dddjava/Jig/releases/latest)から `jig-cli-onejar.jar` をダウンロードしてください。

## 実行方法

```
java -jar jig-cli-onejar.jar
```

`./build/jig` ディレクトリにドキュメントが出力されます。
出力されるドキュメントは[JigDocument](../jig-core/src/main/java/org/dddjava/jig/presentation/view/JigDocument.java)を参照してください。

### プロパティ

次のように `--`に続けて指定します。

```
java -jar jig-cli-onejar.jar --jig.debug=true
```

