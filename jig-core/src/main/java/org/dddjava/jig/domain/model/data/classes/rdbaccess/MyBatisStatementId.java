package org.dddjava.jig.domain.model.data.classes.rdbaccess;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;

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

    String value;

    public MyBatisStatementId(String value) {
        this.value = value;
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

    public boolean matches(MethodDeclarations methodDeclarations) {
        if (value.contains(".")) {
            // 連結されているnamespaceとidを分離する
            var namespace = value.substring(0, value.lastIndexOf('.'));
            var id = value.substring(value.lastIndexOf('.') + 1);

            for (MethodDeclaration methodDeclaration : methodDeclarations.list()) {
                boolean matches = methodDeclaration.matchTypeAndMethodName(TypeIdentifier.valueOf(namespace), id);
                if (matches) return true;
            }
        }
        return false;
    }
}
