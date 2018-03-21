package jig.domain.model.relation;

public class GenericRelation<F, T> {

    F fromIdentifier;
    T toIdentifier;

    public GenericRelation(F fromIdentifier, T toIdentifier) {
        this.fromIdentifier = fromIdentifier;
        this.toIdentifier = toIdentifier;
    }

    public F from() {
        return fromIdentifier;
    }

    public T to() {
        return toIdentifier;
    }
}
