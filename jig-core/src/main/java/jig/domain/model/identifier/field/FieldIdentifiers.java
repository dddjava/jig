package jig.domain.model.identifier.field;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class FieldIdentifiers {

    List<FieldIdentifier> list;

    public FieldIdentifiers(List<FieldIdentifier> list) {
        this.list = list;
    }

    public static Collector<FieldIdentifier, ?, FieldIdentifiers> collector() {
        return Collectors.collectingAndThen(Collectors.toList(), FieldIdentifiers::new);
    }

    public List<FieldIdentifier> list() {
        return list;
    }

    public String toNameText() {
        return list.stream()
                .map(FieldIdentifier::nameText)
                .collect(Collectors.joining(","));
    }

    public String toTypeAndNameText() {
        return list.stream()
                .map(FieldIdentifier::typeAndNameText)
                .collect(Collectors.joining(", "));
    }
}
