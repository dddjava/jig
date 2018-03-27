package jig.domain.model.relation;

public class Depth {
    int value = -1;

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
