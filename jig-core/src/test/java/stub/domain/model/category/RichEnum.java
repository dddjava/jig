package stub.domain.model.category;

import java.util.function.Function;

public enum RichEnum {
    A(111, "A-String-Parameter", a -> a) {
        @Override
        void method() {
            // a
        }
    },
    B(2222, "B-String-Parameter", (b) -> b, "B-String-Parameter-2") {
        @Override
        void method() {
            // b
        }
    };

    private final String param;

    RichEnum(int i, String param, Function<Object, Object> function,
             String arg2) {
        this(i, param, function);
    }

    RichEnum(int i, String param, Function<Object, Object> function) {
        this.param = param;
    }

    abstract void method();
}
