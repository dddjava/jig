package org.dddjava.jig.domain.model.data.persistence;

public record CrudTables(Tables create, Tables read, Tables update, Tables delete) {
}
