package jig.domain.model.definition.field;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class FieldDefinitions {

    List<FieldDefinition> list;

    public FieldDefinitions(List<FieldDefinition> list) {
        this.list = list;
    }

    public static Collector<FieldDefinition, ?, FieldDefinitions> collector() {
        return Collectors.collectingAndThen(Collectors.toList(), FieldDefinitions::new);
    }

    public List<FieldDefinition> list() {
        return list;
    }

    public String toNameText() {
        return list.stream()
                .map(FieldDefinition::nameText)
                .collect(Collectors.joining(","));
    }

    public String toSignatureText() {
        return list.stream()
                .map(FieldDefinition::signatureText)
                .collect(Collectors.joining(", "));
    }
}
