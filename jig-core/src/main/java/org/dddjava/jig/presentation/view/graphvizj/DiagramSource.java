package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.jigdocument.DotText;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ダイアグラムの出力情報
 */
public class DiagramSource {
    List<DotText> values;

    public DiagramSource(List<DotText> values) {
        this.values = values;
    }

    public DiagramSource(DotText dotText) {
        this(Collections.singletonList(dotText));
    }

    public List<DotText> list() {
        return values.stream().filter(dotText -> !dotText.isEmpty()).collect(Collectors.toList());
    }

    public boolean isEmpty() {
        if (values.isEmpty()) {
            return true;
        }
        return values.stream().allMatch(DotText::isEmpty);
    }
}
