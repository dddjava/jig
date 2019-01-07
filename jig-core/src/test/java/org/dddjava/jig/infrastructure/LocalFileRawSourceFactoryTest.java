package org.dddjava.jig.infrastructure;

import org.dddjava.jig.domain.model.implementation.raw.BinarySourceLocations;
import org.dddjava.jig.domain.model.implementation.raw.RawSource;
import org.dddjava.jig.domain.model.implementation.raw.RawSourceLocations;
import org.dddjava.jig.domain.model.implementation.raw.TextSourceLocations;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileRawSourceFactoryTest {

    @Test
    void 読み取れないパスが指定されていてもエラーにならない() {
        RawSourceLocations rawSourceLocations = new RawSourceLocations(
                new BinarySourceLocations(Collections.singletonList(Paths.get("invalid-binary-path"))),
                new TextSourceLocations(Collections.singletonList(Paths.get("invalid-text-path")))
        );

        LocalFileRawSourceFactory sut = new LocalFileRawSourceFactory();
        RawSource source = sut.createSource(rawSourceLocations);

        assertTrue(source.binarySource().classSources().list().isEmpty());
        assertTrue(source.textSource().javaSources().list().isEmpty());
        assertTrue(source.textSource().packageInfoSources().list().isEmpty());
    }
}