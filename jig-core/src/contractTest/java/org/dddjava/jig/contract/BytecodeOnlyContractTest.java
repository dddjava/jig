package org.dddjava.jig.contract;

import org.dddjava.jig.domain.model.documents.JigDocument;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ソースがなくクラスファイルだけを解析できることの契約。
 *
 * ソースを渡せない構成でもサイトは生成でき、失われるのはJavadoc由来の用語だけになる。
 */
class BytecodeOnlyContractTest {

    @TempDir
    static Path workDirectory;

    static Path bytecodeOnlySite;
    static Path withSourcesSite;
    static List<JigDocument> bytecodeOnlyDocuments;

    /**
     * ソースの有無だけが違う二つのサイトを作り、差分を観測する。
     */
    @BeforeAll
    static void サイトを生成する() {
        bytecodeOnlySite = workDirectory.resolve("bytecode-only");
        withSourcesSite = workDirectory.resolve("with-sources");

        bytecodeOnlyDocuments = ShowcaseSite.generateBytecodeOnlyTo(bytecodeOnlySite);
        ShowcaseSite.generateTo(withSourcesSite);
    }

    @Test
    void ソースがなくても全てのドキュメントが生成される() {
        assertTrue(bytecodeOnlyDocuments.containsAll(JigDocument.canonical()),
                () -> "生成されなかったドキュメントがあります: " + bytecodeOnlyDocuments);
    }

    /**
     * 計測値と、Javadocを読み取る用語集、およびソースがないこと自体を記録する診断。
     */
    private static final Set<String> 一致を求めない成果物 =
            Stream.concat(GeneratedSite.MEASUREMENTS.stream(), Stream.of("glossary-data.js", "diagnostics-data.js"))
                    .collect(Collectors.toSet());

    @Test
    void 用語集以外の成果物はソースの有無で変わらない() {
        assertEquals(
                GeneratedSite.normalizedContents(withSourcesSite, 一致を求めない成果物),
                GeneratedSite.normalizedContents(bytecodeOnlySite, 一致を求めない成果物));
    }

    @Test
    void ソースがないことは診断に記録される() {
        String withSources = GeneratedSite.read(withSourcesSite.resolve("data/diagnostics-data.js"));
        String bytecodeOnly = GeneratedSite.read(bytecodeOnlySite.resolve("data/diagnostics-data.js"));

        assertTrue(bytecodeOnly.contains("テキストソースなし"),
                () -> "用語が減った理由を生成物から辿れなくなっています: " + bytecodeOnly);
        assertFalse(withSources.contains("テキストソースなし"), withSources);
    }

    @Test
    void 用語集はJavadoc由来の用語を持たなくなる() {
        String withSources = GeneratedSite.read(withSourcesSite.resolve("data/glossary-data.js"));
        String bytecodeOnly = GeneratedSite.read(bytecodeOnlySite.resolve("data/glossary-data.js"));

        assertTrue(withSources.contains("\"origin\":\"Javadoc\""),
                () -> "ソースがあればJavadoc由来の用語が載る前提が崩れています: " + withSources);
        assertFalse(bytecodeOnly.contains("\"origin\":\"Javadoc\""), bytecodeOnly);
        // 用語がなくても、用語集のデータそのものは壊れずに出力される
        assertTrue(bytecodeOnly.contains("\"domainPackageRoots\":[\"showcase.domain\"]"), bytecodeOnly);
    }
}
