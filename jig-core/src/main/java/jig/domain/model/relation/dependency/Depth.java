package jig.domain.model.relation.dependency;

public class Depth {
    int value;

    public Depth(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public boolean unlimited() {
        return value < 0;
    }
}
