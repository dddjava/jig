package org.dddjava.jig.domain.model.data.persistence;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Objects;

/**
 * 永続化アクセサID
 *
 * namespaceとidを.で連結したもの。
 *
 * TODO idは全体の一意でなくtypeId内の一意なので誤解を招きそう
 */
public record PersistenceAccessorOperationId(String value, TypeId typeId, String id) {

    public static PersistenceAccessorOperationId fromTypeIdAndName(TypeId typeId, String methodName) {
        return new PersistenceAccessorOperationId(typeId.fqn() + "." + methodName, typeId, methodName);
    }

    public boolean matches(TypeId typeId, String name) {
        return typeId.equals(this.typeId) && name.equals(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersistenceAccessorOperationId that = (PersistenceAccessorOperationId) o;
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
