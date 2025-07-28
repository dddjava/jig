package org.dddjava.jig.domain.model.data.rdbaccess;

import org.dddjava.jig.domain.model.information.members.UsingMethods;

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
public class MyBatisStatementId {

    private final String value;
    private final String namespace;
    private final String id;

    public MyBatisStatementId(String value) {
        this.value = value;

        var namespaceIdSeparateIndex = value.lastIndexOf('.');
        if (namespaceIdSeparateIndex != -1) {
            this.namespace = value.substring(0, namespaceIdSeparateIndex);
            this.id = value.substring(namespaceIdSeparateIndex + 1);
        } else {
            this.namespace = "<unknown namespace>";
            this.id = value;
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

    boolean matches(UsingMethods usingMethods) {
        // namespaceはメソッドの型のFQNに該当し、idはメソッド名に該当するので、それを比較する。
        return usingMethods.containsAny(methodCall -> methodCall.methodOwner().fullQualifiedName().equals(namespace()) && methodCall.methodName().equals(id()));
    }

    public String namespace() {
        return namespace;
    }

    public String id() {
        return id;
    }
}
