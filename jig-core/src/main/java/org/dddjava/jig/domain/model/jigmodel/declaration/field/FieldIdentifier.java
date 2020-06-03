package org.dddjava.jig.domain.model.jigmodel.declaration.field;

/**
 * フィールドの名称
 */
public class FieldIdentifier {
    String value;

    public FieldIdentifier(String value) {
        this.value = value;
    }

    public String text() {
        return value;
    }
}
