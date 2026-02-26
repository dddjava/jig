package org.dddjava.jig.domain.model.data.persistence;

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
public record PersistenceOperationId(String value, TypeId typeId, String id) {

    public static PersistenceOperationId from(String value) {
        var namespaceIdSeparateIndex = value.lastIndexOf('.');
        if (namespaceIdSeparateIndex != -1) {
            return new PersistenceOperationId(value, TypeId.valueOf(value.substring(0, namespaceIdSeparateIndex)), value.substring(namespaceIdSeparateIndex + 1));
        } else {
            return new PersistenceOperationId(value, TypeId.valueOf("JigEmptyNamespace"), value);
        }
    }

    public static PersistenceOperationId fromTypeIdAndName(TypeId typeId, String methodName) {
        return new PersistenceOperationId(typeId.fqn() + "." + methodName, typeId, methodName);
    }

    public boolean matches(TypeId typeId, String name) {
        return typeId.equals(this.typeId) && name.equals(this.id);
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
