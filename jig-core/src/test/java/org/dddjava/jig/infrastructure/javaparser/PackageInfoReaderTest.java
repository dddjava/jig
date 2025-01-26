package org.dddjava.jig.infrastructure.javaparser;

import org.dddjava.jig.domain.model.data.packages.PackageComment;
import org.dddjava.jig.domain.model.sources.Sources;
import org.dddjava.jig.domain.model.sources.javasources.ReadableTextSource;
import org.dddjava.jig.domain.model.sources.javasources.ReadableTextSources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import testing.JigTestExtension;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(JigTestExtension.class)
class PackageInfoReaderTest {

    @Test
    void test(Sources sources) throws Exception {
        ReadableTextSources readableTextSources = sources.javaSources().packageInfoSources();
        List<ReadableTextSource> list = readableTextSources.list();
        assertFalse(list.isEmpty(), "0件だったら " + JigTestExtension.class + " がおかしい");
        ReadableTextSource readableTextSource = list.stream()
                .filter(e -> e.path().endsWith(Paths.get("domain", "model", "package-info.java"))).findAny()
                .orElseThrow(AssertionError::new);

        PackageInfoReader sut = new PackageInfoReader();
        PackageComment packageComment = sut.read(readableTextSource)
                .orElseThrow(AssertionError::new);

        assertEquals("スタブドメインモデル", packageComment.asText());
    }
}