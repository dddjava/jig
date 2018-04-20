package jig.domain.model.declaration.field;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class FieldDeclarations {

    List<FieldDeclaration> list;

    public FieldDeclarations(List<FieldDeclaration> list) {
        this.list = list;
    }

    public static Collector<FieldDeclaration, ?, FieldDeclarations> collector() {
        return Collectors.collectingAndThen(Collectors.toList(), FieldDeclarations::new);
    }

    public List<FieldDeclaration> list() {
        return list;
    }

    public String toNameText() {
        return list.stream()
                .map(FieldDeclaration::nameText)
                .collect(Collectors.joining(","));
    }

    public String toSignatureText() {
        return list.stream()
                .map(FieldDeclaration::signatureText)
                .collect(Collectors.joining(", "));
    }
}
