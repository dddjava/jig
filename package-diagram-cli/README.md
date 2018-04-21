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
```

