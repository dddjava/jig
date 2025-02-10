package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.List;

/**
 * JLSにおける `ElementValuePair`
 * @param name elementの名前
 * @param value 現状は単純な値にだけ対応。アノテーションが入ったりもするのでどこまで考慮したものかと。
 * @see <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-9.html#jls-ElementValuePair">...</a>
 */
public record JigAnnotationInstanceElement(String name, JigAnnotationInstanceElementValue value) {

    public static JigAnnotationInstanceElement element(String name, Object value) {
        return new JigAnnotationInstanceElement(name, new JigAnnotationInstanceElementNormalValue(value));
    }

    public static JigAnnotationInstanceElement enumElement(String name, TypeIdentifier typeIdentifier, String value) {
        return new JigAnnotationInstanceElement(name, new JigAnnotationInstanceElementEnumValue(typeIdentifier, value));
    }

    public static JigAnnotationInstanceElement annotationElement(String name, TypeIdentifier typeIdentifier, Collection<JigAnnotationInstanceElement> elements) {
        return new JigAnnotationInstanceElement(name, new JigAnnotationInstanceElementAnnotationValue(typeIdentifier, elements));
    }

    public static JigAnnotationInstanceElement arrayElement(String name, List<JigAnnotationInstanceElementValue> values) {
        return new JigAnnotationInstanceElement(name, new JigAnnotationInstanceElementArray(values));
    }

    public static JigAnnotationInstanceElement classElement(String name, TypeIdentifier typeIdentifier) {
        return new JigAnnotationInstanceElement(name, new JigAnnotationInstanceElementClassValue(typeIdentifier));
    }

    public boolean matchName(String[] elementNames) {
        for (String elementName : elementNames) {
            if (elementName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public String valueAsString() {
        return value.valueText();
    }
}
