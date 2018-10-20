package stub.domain.model.category;

public enum RichEnum {
    A("A") {
        @Override
        void method() {
            // a
        }
    },
    B("B") {
        @Override
        void method() {
            // b
        }
    };

    private final String param;

    RichEnum(String param) {
        this.param = param;
    }

    abstract void method();
}
