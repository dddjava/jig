package jig.domain.model.specification;

public class ClassDescriptor {

    String value;

    public ClassDescriptor(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
