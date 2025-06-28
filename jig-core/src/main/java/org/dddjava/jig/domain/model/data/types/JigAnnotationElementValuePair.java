package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.List;

/**
 * アノテーションの持つ要素。名前と値のペア。
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-9.html#jls-ElementValuePair">...</a>
 */
public record JigAnnotationElementValuePair(String name, JigAnnotationReferenceElementValue value) {

    public static JigAnnotationElementValuePair element(String name, Object value) {
        return new JigAnnotationElementValuePair(name, new JigAnnotationReferenceElementNormalValue(value));
    }

    public static JigAnnotationElementValuePair enumElement(String name, TypeIdentifier typeIdentifier, String value) {
        return new JigAnnotationElementValuePair(name, new JigAnnotationReferenceElementEnumValue(typeIdentifier, value));
    }

    public static JigAnnotationElementValuePair annotationElement(String name, TypeIdentifier typeIdentifier, Collection<JigAnnotationElementValuePair> elements) {
        return new JigAnnotationElementValuePair(name, new JigAnnotationInstanceElementAnnotationValue(typeIdentifier, elements));
    }

    public static JigAnnotationElementValuePair arrayElement(String name, List<JigAnnotationReferenceElementValue> values) {
        return new JigAnnotationElementValuePair(name, new JigAnnotationReferenceElementArray(values));
    }

    public static JigAnnotationElementValuePair classElement(String name, TypeIdentifier typeIdentifier) {
        return new JigAnnotationElementValuePair(name, new JigAnnotationReferenceElementClassValue(typeIdentifier));
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
