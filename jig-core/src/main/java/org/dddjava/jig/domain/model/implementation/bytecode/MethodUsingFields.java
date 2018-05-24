package org.dddjava.jig.domain.model.implementation.bytecode;

import java.util.List;

public class MethodUsingFields {

    List<MethodUsingField> list;

    public MethodUsingFields(List<MethodUsingField> list) {
        this.list = list;
    }

    public MethodUsingFieldStream stream() {
        return new MethodUsingFieldStream(list.stream());
    }
}
