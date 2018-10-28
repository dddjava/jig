package org.dddjava.jig.presentation.view.graphvizj;

import java.util.Collections;
import java.util.List;

public class DotTexts {
    List<DotText> values;

    public DotTexts(List<DotText> values) {
        this.values = values;
    }

    public DotTexts(String value) {
        this(Collections.singletonList(new DotText(value)));
    }

    public List<DotText> list() {
        return values;
    }

    public boolean isEmpty() {
        if (values.isEmpty()) {
            return true;
        }
        return values.stream().allMatch(DotText::isEmpty);
    }
}
