package jig.domain.model.identifier.namespace;

public class PackageDepth {
    int value;

    public PackageDepth(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public boolean unlimited() {
        return value < 0;
    }
}
