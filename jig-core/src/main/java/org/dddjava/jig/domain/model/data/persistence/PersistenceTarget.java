package org.dddjava.jig.domain.model.data.persistence;

/**
 * 永続化操作の操作対象
 *
 * テーブルなど
 */
public record PersistenceTarget(String name) {

    public static PersistenceTarget fromSql(String extractedName) {
        var name = extractedName.replace("\"", "").replace("`", "");
        return new PersistenceTarget(name);
    }
}
