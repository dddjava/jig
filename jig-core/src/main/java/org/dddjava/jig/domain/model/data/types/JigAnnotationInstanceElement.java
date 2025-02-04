package org.dddjava.jig.domain.model.data.types;

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
