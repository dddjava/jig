# package-diagram-cli

パッケージ間の依存図をPNGで出力します。

## 実行

```
java -jar package-diagram-cli.jar 
```

## プロパティ

```
output.diagram.name=output.png
output.omit.prefix=.+\\.(service|domain\\.(model|basic))\\.
project.path=./
depth=-1
jig.diagram.writer=graphviz-java
```

## PlantUMLで出力する場合

`jig.diagram.writer=plantuml` を指定してください。コマンドラインで実行する場合は次のようになります。

```
java -jar package-diagram-cli.jar --jig.diagram.writer=plantuml
```

PlantUMLで出力する場合はGraphVizのインストールが必要になります。

参考: http://plantuml.com/graphviz-dot

### Windows

graphviz-2.38.msi で動作確認しています。

https://graphviz.gitlab.io/_pages/Download/Download_windows.html
