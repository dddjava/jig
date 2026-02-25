package org.dddjava.jig.domain.model.data.rdbaccess;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Objects;

/**
 * 永続化操作ID
 *
 * namespaceとidを.で連結したもの。
 *
 * TODO namespaceやidはMyBatisの用語なのでこの形のままとするかは一考の余地がある
 * TODO idは全体の一意でなくnamespace内の一意なので誤解を招きそう
 */
public record PersistenceOperationId(String value, String namespace, String id) {

    public static PersistenceOperationId from(String value) {
        var namespaceIdSeparateIndex = value.lastIndexOf('.');
        if (namespaceIdSeparateIndex != -1) {
            return new PersistenceOperationId(value, value.substring(0, namespaceIdSeparateIndex), value.substring(namespaceIdSeparateIndex + 1));
        } else {
            return new PersistenceOperationId(value, "<unknown namespace>", value);
        }
    }

    public static PersistenceOperationId fromNamespaceAndId(String namespace, String id) {
        return from(namespace + "." + id);
    }

    public static PersistenceOperationId fromTypeIdAndName(TypeId typeId, String methodName) {
        return fromNamespaceAndId(typeId.fqn(), methodName);
    }

    public boolean matches(String namespace, String name) {
        return namespace.equals(this.namespace) && name.equals(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersistenceOperationId that = (PersistenceOperationId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public String logText() {
        return value;
    }
}
