# learning

JavaParser や JDK の挙動を確認するための調査用コード。品質保証の対象ではなく、CI では実行されない。

`src/test/java` ではなく `src/learning/java` に置いているのは、Gradle の `test` ソースセットが `src/test/java` だけを対象とするため。`learning` という名前の `JvmTestSuite`/ソースセットは定義していないので、ここに置いたコードは**ビルド・実行の対象にならない**。

## 手元で動かす

IDE（IntelliJ 等）であれば `src/learning/java` を通常の Java ディレクトリとして開けば個別に実行できる。

コマンドラインで動かしたい場合は、一時的に `jig-core/build.gradle` へ次のようなソースセットを足す。

```groovy
sourceSets {
    learning {
        java.srcDir('src/learning/java')
        compileClasspath += sourceSets.test.compileClasspath
        runtimeClasspath += sourceSets.test.runtimeClasspath
    }
}

tasks.register('learningTest', Test) {
    testClassesDirs = sourceSets.learning.output.classesDirs
    classpath = sourceSets.learning.runtimeClasspath
    useJUnitPlatform()
}
```

確認が終わったら元に戻すこと。恒久的な仕組みにはしない。

## 昇格

将来この調査結果を仕様として固定したくなった時点で、対象契約に応じた階層（Unit/Component/Contract）へ昇格する。`docs/adr/test_architecture_policy.md` を参照。
