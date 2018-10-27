package org.dddjava.jig.domain.type.usernumber;

public class UserNumber {
    int value;

    public UserNumber(int value) {
        this.value = value;
    }

    public String asText() {
        return Integer.toString(value);
    }
}
