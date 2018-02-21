# Jig

## cli

### 必要な環境

- GraphViz
  - dotを使ってるので http://plantuml.com/graphviz-dot
- JDK(tools.jar)
  - jdepsを使ってるので

### ビルド

```
cd application
./gradlew :cli:build
```

`ls cli/build/libs/cli.jar` ができる

### 実行

```
./cli.jar -source src/main/java build/classes/java/main
```

