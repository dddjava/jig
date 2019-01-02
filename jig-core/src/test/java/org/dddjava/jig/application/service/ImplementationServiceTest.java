package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.implementation.raw.BinarySources;
import org.dddjava.jig.domain.model.implementation.raw.RawSource;
import org.dddjava.jig.domain.model.implementation.raw.TextSources;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JigServiceTest
public class ImplementationServiceTest {

    @Test
    void 対象ソースなし(ImplementationService implementationService) {
        RawSource rawSource = new RawSource(new TextSources(Collections.emptyList()), new BinarySources(Collections.emptyList()));

        assertThatThrownBy(() -> implementationService.readProjectData(rawSource))
                .isInstanceOf(ClassFindFailException.class);
    }
}
