package jig.application.service;

import jig.domain.model.project.ProjectLocation;
import jig.domain.model.specification.SpecificationReader;
import jig.infrastructure.JigPaths;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class AnalyzeServiceUnitTest {

    @Test
    void 解析対象が見つからない場合は例外を投げて処理を終了する() {
        JigPaths jigPaths = new JigPaths(
                "not/match/any/directory",
                "not/match/any/directory",
                "not/match/any/directory");
        SpecificationReader specificationReaderMock = mock(SpecificationReader.class);

        AnalyzeService sut = new AnalyzeService(
                jigPaths,
                new SpecificationService(specificationReaderMock), null, null, null);

        ProjectLocation location = new ProjectLocation(Paths.get(""));

        assertThatThrownBy(() -> sut.importProject(location))
                .isInstanceOf(RuntimeException.class);
        verifyZeroInteractions(specificationReaderMock);
    }
}
