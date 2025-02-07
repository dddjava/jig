package org.dddjava.jig.domain.model.data.classes.field;

import java.util.List;
import java.util.stream.Collectors;

public class JigFields {
    List<JigField> list;

    public JigFields(List<JigField> list) {
        this.list = list;
    }

    public boolean empty() {
        return list.isEmpty();
    }

    public FieldDeclarations fieldDeclarations() {
        return new FieldDeclarations(list.stream().map(jigField -> jigField.fieldDeclaration).collect(Collectors.toList()));
    }

    public List<JigField> list() {
        return list;
    }
}
