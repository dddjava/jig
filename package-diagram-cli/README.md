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

- ビルド時にPlantUMLを含めるように指定する
- 実行環境にGraphVizをインストールする
- 実行時にプロパティを指定する

### ビルド

```
./gradlew clean build -PincludePlantUML
```

`plantuml.jar` が同梱された `package-diagram-cli.jar` が作成されます。

### 実行環境

GraphVizのインストールが必要になります。
Windowsは graphviz-2.38.msi で動作確認しています。

- http://plantuml.com/graphviz-dot
- https://graphviz.gitlab.io/_pages/Download/Download_windows.html

### 実行時

`jig.diagram.writer=plantuml` を指定してください。コマンドラインで実行する場合は次のようになります。

```
java -jar package-diagram-cli.jar --jig.diagram.writer=plantuml
```

