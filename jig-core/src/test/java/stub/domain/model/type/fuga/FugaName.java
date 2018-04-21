package stub.domain.model.type.fuga;

public class FugaName {

    String value;

    FugaName() {
    }

    public FugaName(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
