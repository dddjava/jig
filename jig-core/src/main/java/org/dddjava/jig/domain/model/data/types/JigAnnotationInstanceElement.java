package org.dddjava.jig.domain.model.data.types;

/**
 * JLSにおける `ElementValuePair`
 * @param name
 * @param value
 * @see <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-9.html#jls-ElementValuePair">...</a>
 */
public record JigAnnotationInstanceElement(String name, Object value) {

    public boolean matchName(String[] elementNames) {
        for (String elementName : elementNames) {
            if (elementName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public String valueAsString() {
        return String.valueOf(value);
    }
}
