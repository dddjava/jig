package org.dddjava.jig.domain.model.information.outputs.springdata;

import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.data.persistence.*;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;

public class SpringDataUtil {
    private static final Logger logger = LoggerFactory.getLogger(SpringDataUtil.class);
    private static final String SPRING_DATA_REPOSITORY_PREFIX = "org.springframework.data.repository.";

    /**
     * SQLの種類を推測する
     *
     * @see <a href="https://docs.spring.io/spring-data/relational/reference/data-commons/repositories/query-methods-details.html">Defining Query Methods</a>
     */
    public static Optional<SqlType> inferSqlType(String methodName) {
        String normalizedMethodName = methodName.toLowerCase(Locale.ROOT);

        if (normalizedMethodName.startsWith("find")
                || normalizedMethodName.startsWith("read")
                || normalizedMethodName.startsWith("get")
                || normalizedMethodName.startsWith("query")
                || normalizedMethodName.startsWith("count")
                || normalizedMethodName.startsWith("exists")) {
            return Optional.of(SqlType.SELECT);
        }
        if (normalizedMethodName.startsWith("save")
                || normalizedMethodName.startsWith("insert")
                || normalizedMethodName.startsWith("create")
                || normalizedMethodName.startsWith("add")) {
            return Optional.of(SqlType.INSERT);
        }
        if (normalizedMethodName.startsWith("update")
                || normalizedMethodName.startsWith("set")) {
            return Optional.of(SqlType.UPDATE);
        }
        if (normalizedMethodName.startsWith("delete")
                || normalizedMethodName.startsWith("remove")) {
            return Optional.of(SqlType.DELETE);
        }

        // 判別できないものは空にしておく
        logger.info("SQLの種類がメソッド名 {} から判別できませんでした。CRUDのどれかに該当する場合は対象にしたいのでissueお願いします。", methodName);
        return Optional.empty();
    }

    /**
     * 呼び出しているメソッドから組み上げる
     *
     * MethodCallが存在する以上はコンパイルが通っているので、解決済みの永続化操作がなくても継承しているIFなどで定義されている可能性が高い。
     * 主なユースケースはSpringDataJDBCのCrudRepositoryなどに定義されたメソッドを呼び出し元から「存在するもの」として構築すること。
     */
    public static Optional<PersistenceAccessor> generateCalledPersistenceOperation(MethodCall methodCall,
                                                                                   PersistenceAccessors persistenceAccessors) {
        if (persistenceAccessors.technology() != PersistenceAccessorTechnology.SPRING_DATA_JDBC) {
            return Optional.empty();
        }

        // SpringDataJDBCのIFに定義されたメソッドの解決を試みる
        PersistenceAccessorId persistenceAccessorId = generatedPersistenceOperationId(methodCall, persistenceAccessors);
        return inferSqlType(methodCall.methodName())
                .map(sqlType -> PersistenceAccessor.from(
                        persistenceAccessorId,
                        sqlType,
                        persistenceAccessors.defaultPersistenceTargets()));
    }

    public static PersistenceAccessorId toPersistenceOperationId(MethodCall methodCall) {
        return PersistenceAccessorId.fromTypeIdAndName(methodCall.methodOwner(), methodCall.methodName());
    }

    private static PersistenceAccessorId generatedPersistenceOperationId(MethodCall methodCall,
                                                                         PersistenceAccessors persistenceAccessors) {
        if (isSpringDataRepositoryType(methodCall.methodOwner())) {
            return PersistenceAccessorId.fromTypeIdAndName(persistenceAccessors.typeId(), methodCall.methodName());
        }
        return toPersistenceOperationId(methodCall);
    }

    public static boolean isSpringDataRepositoryType(TypeId typeId) {
        return typeId.fqn().startsWith(SPRING_DATA_REPOSITORY_PREFIX);
    }
}
