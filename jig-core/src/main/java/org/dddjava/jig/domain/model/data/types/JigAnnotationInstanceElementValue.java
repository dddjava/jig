package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

public sealed interface JigAnnotationInstanceElementValue permits
        JigAnnotationInstanceElementNormalValue,
        JigAnnotationInstanceElementClassValue,
        JigAnnotationInstanceElementEnumValue,
        JigAnnotationInstanceElementAnnotationValue,
        JigAnnotationInstanceElementArray {
    String valueText();
}

record JigAnnotationInstanceElementArray(List<JigAnnotationInstanceElementValue> values)
        implements JigAnnotationInstanceElementValue {
    @Override
    public String valueText() {
        return values.stream().map(JigAnnotationInstanceElementValue::valueText).collect(joining(", ", "{", "}"));
    }
}

record JigAnnotationInstanceElementNormalValue(Object value)
        implements JigAnnotationInstanceElementValue {
    @Override
    public String valueText() {
        return Objects.toString(value);
    }
}
record JigAnnotationInstanceElementClassValue(TypeIdentifier value)
        implements JigAnnotationInstanceElementValue {
    @Override
    public String valueText() {
        return value.asSimpleName();
    }
}

record JigAnnotationInstanceElementEnumValue(TypeIdentifier typeIdentifier, String constantName)
        implements JigAnnotationInstanceElementValue {
    @Override
    public String valueText() {
        return typeIdentifier.asSimpleName() + "." + constantName;
    }
}

record JigAnnotationInstanceElementAnnotationValue(TypeIdentifier typeIdentifier,
                                                   Collection<JigAnnotationInstanceElement> elements)
        implements JigAnnotationInstanceElementValue {
    @Override
    public String valueText() {
        return "@" + typeIdentifier.asSimpleName() + "(...)";
    }
}
