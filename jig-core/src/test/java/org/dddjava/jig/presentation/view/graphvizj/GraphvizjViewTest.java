package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.jigdocument.DotText;
import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

class GraphvizjViewTest {

    @MethodSource
    @ParameterizedTest
    void test(DotTexts dotTexts) {
        DotTextEditor<Object> editor = obj -> dotTexts;


        GraphvizjView<Object> sut = new GraphvizjView<>(editor, DiagramFormat.SVG);

        JigDocumentWriter jigDocumentWriter = mock(JigDocumentWriter.class);
        Object model = null;
        sut.render(model, jigDocumentWriter);
    }

    static Stream<DotTexts> test() {
        return Stream.of(
                new DotTexts(Collections.emptyList()),
                new DotTexts(DotText.empty()),
                new DotTexts(new DotText("dummy")),
                new DotTexts(Arrays.asList(DotText.empty(), DotText.empty())),
                new DotTexts(Arrays.asList(DotText.empty(), new DotText("dummy")))
        );
    }
}