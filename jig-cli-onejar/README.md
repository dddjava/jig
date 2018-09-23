# JIG: コマンドライン版（SpringBoot未使用）

以下のドキュメントを出力するコマンドラインツールのSpringBootを使用しない実験版です。

## 実行方法

```
java -jar jig-cli-onejar.jar
```

`./build/jig` ディレクトリにドキュメントが出力されます。

### プロパティ

次のように `--`に続けて指定します。

```
java -jar jig-cli-onejar.jar --jig.debug=true
```

