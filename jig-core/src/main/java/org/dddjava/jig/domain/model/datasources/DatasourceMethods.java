package org.dddjava.jig.domain.model.datasources;

import java.util.List;

public class DatasourceMethods {
    List<DatasourceMethod> list;

    public DatasourceMethods(List<DatasourceMethod> list) {
        this.list = list;
    }

    public List<DatasourceMethod> list() {
        return list;
    }

    public boolean empty() {
        return list.isEmpty();
    }
}
