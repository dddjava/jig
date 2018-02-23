
## 必要な環境

- GraphViz
  - dotを使ってるので http://plantuml.com/graphviz-dot
- JDK(tools.jar)
  - jdepsを使ってるので

## ビルド

```
cd application
./gradlew clean :package-diagram-cli:build :class-list-cli:build
```

## 実行

できたjarファイルを `java -jar` で実行。

```
./package-diagram-cli.jar --target.source=src/main/java --target.class=build/classes/java/main
```

その他は `application.properties` 参照。

