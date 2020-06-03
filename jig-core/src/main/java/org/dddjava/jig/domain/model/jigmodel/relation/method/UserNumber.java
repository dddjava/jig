package org.dddjava.jig.domain.model.jigmodel.relation.method;

/**
 * 使用箇所数
 */
public class UserNumber {
    int value;

    public UserNumber(int value) {
        this.value = value;
    }

    public String asText() {
        return Integer.toString(value);
    }
}
