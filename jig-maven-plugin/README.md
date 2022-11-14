jig-maven-plugin
============================================================

## 簡易実行

```
mvn org.dddjava.jig:jig-maven-plugin:jig
```

`pom.xml` には何も書かなくていいです。

## 通常

プラグインのバージョンとか設定を `/project/build/plugins` に追加

```pom.xml
<project>
    ...
    <build>
        ...
        <plugins>
            ...
            <plugin>
                <groupId>org.dddjava.jig</groupId>
                <artifactId>jig-maven-plugin</artifactId>
                <version>{jig.version}</version>
                <configuration>
                    <domainPattern>.*</domainPattern>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

実行

```
mvn jig:jig
```

# 設定

|対象|configurationタグ名|プロパティ名|
|----|----|----|
|出力対象JIGドキュメント| `documentTypes` | `jig.document.types` |
|ドメインのパターン| `domainPattern` | `jig.pattern.domain` |

ともに任意。指定なしの場合はJIGのデフォルトに従う。
