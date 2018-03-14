package sut.domain.model.kind;

public enum ParameterizedEnum {
    A("a"),
    B("b");

    private final String param;

    ParameterizedEnum(String param) {
        this.param = param;
    }

    public String getParam() {
        return param;
    }
}
