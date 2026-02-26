package org.dddjava.jig.domain.model.data.persistence;

public record CrudTables(PersistenceTargets create, PersistenceTargets read, PersistenceTargets update, PersistenceTargets delete) {
}
