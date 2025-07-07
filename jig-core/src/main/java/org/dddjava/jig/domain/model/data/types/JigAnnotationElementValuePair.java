package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.List;

/**
 * アノテーションの持つ要素。名前と値のペア。
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-9.html#jls-ElementValuePair">...</a>
 */
public record JigAnnotationElementValuePair(String name, JigAnnotationElementValue value) {

    public static JigAnnotationElementValuePair element(String name, Object value) {
        return new JigAnnotationElementValuePair(name, new JigAnnotationElementNormalValue(value));
    }

    public static JigAnnotationElementValuePair enumElement(String name, TypeId typeId, String value) {
        return new JigAnnotationElementValuePair(name, new JigAnnotationElementEnumValue(typeId, value));
    }

    public static JigAnnotationElementValuePair annotationElement(String name, TypeId typeId, Collection<JigAnnotationElementValuePair> elements) {
        return new JigAnnotationElementValuePair(name, new JigAnnotationInstanceElementAnnotationValue(typeId, elements));
    }

    public static JigAnnotationElementValuePair arrayElement(String name, List<JigAnnotationElementValue> values) {
        return new JigAnnotationElementValuePair(name, new JigAnnotationElementArray(values));
    }

    public static JigAnnotationElementValuePair classElement(String name, TypeId typeId) {
        return new JigAnnotationElementValuePair(name, new JigAnnotationElementClassValue(typeId));
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
