package org.dddjava.jig.domain.model.knowledge.datasource;

import org.dddjava.jig.domain.model.data.rdbaccess.Tables;

public record CrudTables(Tables create, Tables read, Tables update, Tables delete) {
}
