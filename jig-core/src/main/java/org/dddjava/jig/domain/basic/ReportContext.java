package org.dddjava.jig.domain.basic;

public class ReportContext {

    private Object value;

    public ReportContext(Object value) {
        this.value = value;
    }

    public <T> T value() {
        return (T) value;
    }
}
