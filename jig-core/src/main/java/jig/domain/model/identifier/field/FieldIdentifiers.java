package jig.domain.model.identifier.field;

import java.util.List;

public class FieldIdentifiers {

    List<FieldIdentifier> list;

    public FieldIdentifiers(List<FieldIdentifier> list) {
        this.list = list;
    }

    public List<FieldIdentifier> list() {
        return list;
    }
}
