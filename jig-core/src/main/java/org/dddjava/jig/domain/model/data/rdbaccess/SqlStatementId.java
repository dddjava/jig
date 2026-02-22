package org.dddjava.jig.domain.model.data.rdbaccess;

import java.util.Objects;

/**
 * SQLステートメントID
 *
 * namespaceとidを.で連結したもの。
 *
 * TODO namespaceやidはMyBatisの用語なのでこの形のままとするかは一考の余地がある
 */
public record SqlStatementId(String value, String namespace, String id) {

    public static SqlStatementId from(String value) {
        var namespaceIdSeparateIndex = value.lastIndexOf('.');
        if (namespaceIdSeparateIndex != -1) {
            return new SqlStatementId(value, value.substring(0, namespaceIdSeparateIndex), value.substring(namespaceIdSeparateIndex + 1));
        } else {
            return new SqlStatementId(value, "<unknown namespace>", value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SqlStatementId that = (SqlStatementId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public String namespace() {
        return namespace;
    }

    public String id() {
        return id;
    }
}
