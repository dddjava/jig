# package-diagram-cli

パッケージ間の依存図をPNGで出力します。

## 実行

```
java -jar package-diagram-cli.jar 
```

## プロパティ

```
output.diagram.name=output.png
package.pattern=.*.domain.model
target.class=./sut/build/classes/java/main
target.source=./sut/src/main/java
```
