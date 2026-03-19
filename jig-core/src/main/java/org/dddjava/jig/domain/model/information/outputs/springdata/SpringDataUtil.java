package org.dddjava.jig.domain.model.information.outputs.springdata;

import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationType;
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
    public static Optional<PersistenceOperationType> inferOperationType(String methodName) {
        String normalizedMethodName = methodName.toLowerCase(Locale.ROOT);

        if (normalizedMethodName.startsWith("find")
                || normalizedMethodName.startsWith("read")
                || normalizedMethodName.startsWith("get")
                || normalizedMethodName.startsWith("query")
                || normalizedMethodName.startsWith("count")
                || normalizedMethodName.startsWith("exists")) {
            return Optional.of(PersistenceOperationType.SELECT);
        }
        // TODO saveとsaveAllはINSERTとUPDATEの区別が静的にはできない。ここではINSERTに寄せておき、ドキュメントの注意書きか何かでフォローする。
        if (normalizedMethodName.startsWith("save")
                || normalizedMethodName.startsWith("insert")
                || normalizedMethodName.startsWith("create")
                || normalizedMethodName.startsWith("add")) {
            return Optional.of(PersistenceOperationType.INSERT);
        }
        if (normalizedMethodName.startsWith("update")
                || normalizedMethodName.startsWith("set")) {
            return Optional.of(PersistenceOperationType.UPDATE);
        }
        if (normalizedMethodName.startsWith("delete")
                || normalizedMethodName.startsWith("remove")) {
            return Optional.of(PersistenceOperationType.DELETE);
        }

        // 判別できないものは空にしておく
        logger.info("SQLの種類がメソッド名 {} から判別できませんでした。CRUDのどれかに該当する場合は対象にしたいのでissueお願いします。", methodName);
        return Optional.empty();
    }

    public static boolean isSpringDataRepositoryType(TypeId typeId) {
        return typeId.fqn().startsWith(SPRING_DATA_REPOSITORY_PREFIX);
    }
}
