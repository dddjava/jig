package stub.domain.model.smell;

public class SmelledClass {

    void returnVoid() {
    }

    int returnInt() {
        return 0;
    }

    long returnLong() {
        return 0;
    }

    boolean returnBoolean() {
        return false;
    }

    SmelledClass intParameter(int i) {
        return new SmelledClass();
    }

    SmelledClass longParameter(long l) {
        return new SmelledClass();
    }

    SmelledClass booleanParameter(boolean b) {
        return new SmelledClass();
    }

    SmelledClass useNullLiteral() {
        return null;
    }

    SmelledClass judgeNull() {
        return toString() == null ? this : new SmelledClass();
    }
}
