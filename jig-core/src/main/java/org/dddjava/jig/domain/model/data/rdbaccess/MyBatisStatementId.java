package org.dddjava.jig.domain.model.data.rdbaccess;

import java.util.Objects;

/**
 * MyBatisのステートメントID
 *
 * namespaceとidを.で連結したもの。
 *
 * 以下のMapperXMLとMapperインタフェースの場合、ステートメントIDは `com.example.mybatis.ExampleMapper.selectAll` となります。
 * <pre>
 * {@code
 * <mapper namespace="com.example.mybatis.ExampleMapper">
 *     <select id="selectAll">
 *         SELECT * FROM EXAMPLE
 *     </select>
 * </mapper>
 * }
 * </pre>
 * <pre>
 * {@code
 * package com.example.mybatis;
 * interface ExampleMapper {
 *     List selectAll();
 * }
 * }
 * </pre>
 */
public record MyBatisStatementId(String value, String namespace, String id) {

    public static MyBatisStatementId from(String value) {
        var namespaceIdSeparateIndex = value.lastIndexOf('.');
        if (namespaceIdSeparateIndex != -1) {
            return new MyBatisStatementId(value, value.substring(0, namespaceIdSeparateIndex), value.substring(namespaceIdSeparateIndex + 1));
        } else {
            return new MyBatisStatementId(value, "<unknown namespace>", value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyBatisStatementId that = (MyBatisStatementId) o;
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
