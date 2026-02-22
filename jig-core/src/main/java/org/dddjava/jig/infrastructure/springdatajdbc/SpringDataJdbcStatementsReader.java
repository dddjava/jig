package org.dddjava.jig.infrastructure.springdatajdbc;

import org.dddjava.jig.domain.model.data.rdbaccess.*;
import org.dddjava.jig.domain.model.data.types.JavaTypeDeclarationKind;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.JigMethodDeclaration;
import org.dddjava.jig.infrastructure.asm.ClassDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.stream.Collectors.toMap;

public class SpringDataJdbcStatementsReader {
    private static final Logger logger = LoggerFactory.getLogger(SpringDataJdbcStatementsReader.class);

    private static final String SPRING_DATA_REPOSITORY_PREFIX = "org.springframework.data.repository.";
    private static final String SPRING_DATA_TABLE = "org.springframework.data.relational.core.mapping.Table";
    private static final String SPRING_DATA_QUERY_ANNOTATION = "org.springframework.data.jdbc.repository.query.Query";

    /**
     * ASMで読み取ったクラス情報から、Spring Data JDBCのRepositoryメソッドをSQLステートメントとして抽出する。
     *
     * 対象は次の条件をすべて満たす型:
     * 1) interface である
     * 2) 継承先（再帰含む）に {@code org.springframework.data.repository.*} を持つ
     */
    public SqlStatements readFrom(Collection<ClassDeclaration> classDeclarations) {
        Map<TypeId, ClassDeclaration> declarationMap = classDeclarations.stream()
                .collect(toMap(
                        declaration -> declaration.jigTypeHeader().id(),
                        declaration -> declaration,
                        (left, right) -> left,
                        LinkedHashMap::new));

        Map<SqlStatementId, SqlStatement> statements = new LinkedHashMap<>();

        classDeclarations.stream()
                .filter(this::isInterface)
                .filter(declaration -> extendsSpringDataRepository(declaration.jigTypeHeader(), declarationMap, new HashSet<>()))
                .forEach(declaration -> {
                    Optional<String> tableName = resolveTableName(declaration.jigTypeHeader(), declarationMap, new HashSet<>());
                    Map<String, Query> queryByMethodName = declaration.jigMethodDeclarations().stream()
                            .collect(toMap(
                                    methodDeclaration -> methodDeclaration.header().name(),
                                    methodDeclaration -> resolveQueryFromAnnotation(methodDeclaration),
                                    (left, right) -> left.supported() ? left : right.supported() ? right : left,
                                    LinkedHashMap::new))
                            .entrySet().stream()
                            .filter(entry -> entry.getValue().supported())
                            .collect(toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (left, right) -> left,
                                    LinkedHashMap::new));

                    declaration.jigMethodDeclarations().stream()
                            .map(jigMethodDeclaration -> jigMethodDeclaration.header().name())
                            .distinct()
                            .forEach(methodName -> {
                                Query query = queryByMethodName.getOrDefault(methodName, Query.unsupported());
                                Optional<SqlType> inferredSqlType = query.supported()
                                        ? SqlType.inferSqlTypeFromQuery(query)
                                        : inferSqlType(methodName);
                                inferredSqlType.ifPresent(sqlType -> {
                                    Query resolvedQuery = query.supported()
                                            ? query
                                            : tableName.map(name -> Query.from(defaultQuery(sqlType, name))).orElse(Query.unsupported());
                                    String statementValue = declaration.jigTypeHeader().fqn() + "." + methodName;
                                    SqlStatementId statementId = SqlStatementId.from(statementValue);
                                    statements.put(statementId, new SqlStatement(statementId, resolvedQuery, sqlType));
                                });
                            });
                });

        return new SqlStatements(List.copyOf(statements.values()));
    }

    /**
     * SpringDataJDBCのQueryアノテーションからクエリを取得する
     *
     * アノテーションがない場合は `Query.unsupported()` になる
     */
    private static Query resolveQueryFromAnnotation(JigMethodDeclaration methodDeclaration) {
        return methodDeclaration.header().declarationAnnotationStream()
                .filter(annotation -> annotation.id().fqn().equals(SPRING_DATA_QUERY_ANNOTATION))
                .findFirst()
                .flatMap(annotation -> annotation.elementTextOf("value"))
                .map(Query::from)
                .orElse(Query.unsupported());
    }

    private boolean isInterface(ClassDeclaration declaration) {
        JigTypeHeader header = declaration.jigTypeHeader();
        return header.javaTypeDeclarationKind() == JavaTypeDeclarationKind.INTERFACE;
    }

    private boolean extendsSpringDataRepository(JigTypeHeader header, Map<TypeId, ClassDeclaration> declarationMap, Set<TypeId> visited) {
        if (!visited.add(header.id())) return false;

        for (JigTypeReference interfaceType : header.interfaceTypeList()) {
            TypeId interfaceId = interfaceType.id();
            // CrudRepository / PagingAndSortingRepository / Repository などを包含するプレフィックス判定
            if (interfaceId.fqn().startsWith(SPRING_DATA_REPOSITORY_PREFIX)) return true;

            ClassDeclaration declaration = declarationMap.get(interfaceId);
            if (declaration != null && extendsSpringDataRepository(declaration.jigTypeHeader(), declarationMap, visited)) {
                return true;
            }
        }
        return false;
    }

    private Optional<String> resolveTableName(JigTypeHeader repositoryHeader, Map<TypeId, ClassDeclaration> declarationMap, Set<TypeId> visited) {
        Optional<TypeId> entityTypeId = resolveEntityTypeId(repositoryHeader, declarationMap, visited);
        if (entityTypeId.isEmpty()) return Optional.empty();

        TypeId typeId = entityTypeId.orElseThrow();
        ClassDeclaration entityDeclaration = declarationMap.get(typeId);
        if (entityDeclaration == null) {
            return Optional.of(toSnakeCase(typeId.asSimpleText()));
        }

        Optional<String> tableName = entityDeclaration.jigTypeHeader().jigTypeAttributes().declarationAnnotationInstances().stream()
                .filter(annotation -> annotation.id().fqn().equals(SPRING_DATA_TABLE))
                .findFirst()
                .flatMap(annotation -> annotation.elementTextOf("value"))
                .filter(value -> !value.isBlank());

        if (tableName.isPresent()) return tableName;
        return Optional.of(toSnakeCase(entityDeclaration.jigTypeHeader().simpleName()));
    }

    private Optional<TypeId> resolveEntityTypeId(JigTypeHeader header, Map<TypeId, ClassDeclaration> declarationMap, Set<TypeId> visited) {
        if (!visited.add(header.id())) return Optional.empty();

        for (JigTypeReference interfaceType : header.interfaceTypeList()) {
            TypeId interfaceId = interfaceType.id();
            if (interfaceId.fqn().startsWith(SPRING_DATA_REPOSITORY_PREFIX)
                    && !interfaceType.typeArgumentList().isEmpty()) {
                // Repository<T, ID> の先頭型引数Tをエンティティ型として扱う
                return Optional.of(interfaceType.typeArgumentList().getFirst().typeId());
            }

            ClassDeclaration declaration = declarationMap.get(interfaceId);
            if (declaration != null) {
                Optional<TypeId> entityTypeId = resolveEntityTypeId(declaration.jigTypeHeader(), declarationMap, visited);
                if (entityTypeId.isPresent()) return entityTypeId;
            }
        }
        return Optional.empty();
    }

    /**
     * SQLの種類を推測する
     *
     * @see <a href="https://docs.spring.io/spring-data/relational/reference/data-commons/repositories/query-methods-details.html">Defining Query Methods</a>
     */
    private Optional<SqlType> inferSqlType(String methodName) {
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

    private String defaultQuery(SqlType sqlType, String tableName) {
        return switch (sqlType) {
            case INSERT -> "insert into " + tableName + " values (?)";
            case SELECT -> "select * from " + tableName;
            case UPDATE -> "update " + tableName + " set id = id";
            case DELETE -> "delete from " + tableName;
        };
    }

    private String toSnakeCase(String text) {
        StringBuilder builder = new StringBuilder(text.length() + 4);
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (Character.isUpperCase(current) && i > 0) builder.append('_');
            builder.append(Character.toLowerCase(current));
        }
        return builder.toString();
    }
}
