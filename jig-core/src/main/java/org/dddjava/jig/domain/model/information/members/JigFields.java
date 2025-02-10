package org.dddjava.jig.domain.model.information.members;

import java.util.List;

public class JigFields {
    List<JigField> list;

    public JigFields(List<JigField> list) {
        this.list = list;
    }

    public boolean empty() {
        return list.isEmpty();
    }

    public List<JigField> list() {
        return list;
    }
}
