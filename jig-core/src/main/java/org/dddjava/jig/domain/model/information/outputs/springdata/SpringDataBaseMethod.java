package org.dddjava.jig.domain.model.information.outputs.springdata;

import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationType;

import java.util.stream.Stream;

/**
 * Spring Data JDBCで規定されているメソッド
 */
public enum SpringDataBaseMethod {
    SAVE("save", PersistenceOperationType.INSERT),
    SAVE_ALL("saveAll", PersistenceOperationType.INSERT),
    FIND_BY_ID("findById", PersistenceOperationType.SELECT),
    FIND_ALL("findAll", PersistenceOperationType.SELECT),
    FIND_ALL_BY_ID("findAllById", PersistenceOperationType.SELECT),
    EXISTS_BY_ID("existsById", PersistenceOperationType.SELECT),
    COUNT("count", PersistenceOperationType.SELECT),
    DELETE_BY_ID("deleteById", PersistenceOperationType.DELETE),
    DELETE("delete", PersistenceOperationType.DELETE),
    DELETE_ALL_BY_ID("deleteAllById", PersistenceOperationType.DELETE),
    DELETE_ALL("deleteAll", PersistenceOperationType.DELETE);

    private final String methodName;
    private final PersistenceOperationType persistenceOperationType;

    SpringDataBaseMethod(String methodName, PersistenceOperationType persistenceOperationType) {
        this.methodName = methodName;
        this.persistenceOperationType = persistenceOperationType;
    }

    public String methodName() {
        return methodName;
    }

    public PersistenceOperationType persistenceOperationType() {
        return persistenceOperationType;
    }

    public static Stream<SpringDataBaseMethod> stream() {
        return Stream.of(values());
    }
}
