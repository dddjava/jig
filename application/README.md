
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

詳細はそれぞれの `README.md` を参照。