package stub.domain.model.smell;

public class SmellMethods {

    int returnInt() {
        return 0;
    }

    long returnLong() {
        return 0;
    }

    boolean returnBoolean() {
        return false;
    }

    SmellMethods intParameter(int i) {
        return new SmellMethods();
    }

    SmellMethods longParameter(long l) {
        return new SmellMethods();
    }

    SmellMethods booleanParameter(boolean b) {
        return new SmellMethods();
    }

    SmellMethods useNullLiteral() {
        return null;
    }

    SmellMethods judgeNull() {
        return this == null ? new SmellMethods() : new SmellMethods();
    }
}
