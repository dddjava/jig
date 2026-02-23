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
import java.util.function.Function;
import java.util.stream.Stream;

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
                        Function.identity(),
                        (left, right) -> right));

        Map<SqlStatementId, SqlStatement> statements = classDeclarations.stream()
                .filter(this::isInterface)
                .flatMap(declaration -> resolveSpringDataRepositoryInfo(declaration.jigTypeHeader(), declarationMap, new HashSet<>())
                        .stream()
                        .flatMap(repositoryInfo -> extractSqlStatements(declaration, repositoryInfo.entityTypeId(), declarationMap)))
                .collect(toMap(
                        SqlStatement::sqlStatementId,
                        Function.identity(),
                        (left, right) -> right));

        return new SqlStatements(List.copyOf(statements.values()));
    }

    private Stream<SqlStatement> extractSqlStatements(ClassDeclaration declaration, Optional<TypeId> entityTypeId, Map<TypeId, ClassDeclaration> declarationMap) {
        Optional<Tables> resolvedTables = resolveTablesFromEntityTableAnnotation(entityTypeId, declarationMap);

        return declaration.jigMethodDeclarations().stream()
                .map(jigMethodDeclaration -> {
                    String methodName = jigMethodDeclaration.header().name();
                    Query query = resolveQueryFromAnnotation(jigMethodDeclaration);
                    Optional<SqlType> inferredSqlType = query.supported()
                            ? SqlType.inferSqlTypeFromQuery(query)
                            : inferSqlType(methodName);
                    return inferredSqlType.map(sqlType -> {
                        SqlStatementId statementId = SqlStatementId.fromNamespaceAndId(declaration.jigTypeHeader().fqn(), methodName);
                        // クエリがあればクエリを優先
                        if (query.supported()) {
                            return new SqlStatement(statementId, query, sqlType);
                        }
                        return resolvedTables
                                // クエリなしは @Table で記述されているもの
                                .map(tables -> new SqlStatement(statementId, Query.unsupported(), sqlType, tables))
                                // クエリもテーブルも見当たらないもの
                                // TODO これはSqlStatementではないのでは？
                                .orElseGet(() -> new SqlStatement(statementId, Query.unsupported(), sqlType));
                    });
                })
                .flatMap(Optional::stream);
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

    private Optional<SpringDataRepositoryInfo> resolveSpringDataRepositoryInfo(JigTypeHeader header, Map<TypeId, ClassDeclaration> declarationMap, Set<TypeId> visited) {
        if (!visited.add(header.id())) return Optional.empty();

        for (JigTypeReference interfaceType : header.interfaceTypeList()) {
            TypeId interfaceId = interfaceType.id();
            // CrudRepository / PagingAndSortingRepository / Repository などを包含するプレフィックス判定
            if (isSpringDataRepository(interfaceId)) {
                Optional<TypeId> entityTypeId = interfaceType.typeArgumentList().isEmpty()
                        ? Optional.empty()
                        : Optional.of(interfaceType.typeArgumentList().getFirst().typeId());
                return Optional.of(new SpringDataRepositoryInfo(entityTypeId));
            }

            ClassDeclaration declaration = declarationMap.get(interfaceId);
            if (declaration != null) {
                Optional<SpringDataRepositoryInfo> repositoryInfo = resolveSpringDataRepositoryInfo(declaration.jigTypeHeader(), declarationMap, visited);
                if (repositoryInfo.isPresent()) return repositoryInfo;
            }
        }
        return Optional.empty();
    }

    private static Optional<Tables> resolveTablesFromEntityTableAnnotation(Optional<TypeId> entityTypeId, Map<TypeId, ClassDeclaration> declarationMap) {
        // TODO: @MappedCollection などを辿って複数テーブル引っ張れるようにする
        return entityTypeId.map(typeId -> {
                    ClassDeclaration entityDeclaration = declarationMap.get(typeId);
                    String tableName;
                    if (entityDeclaration == null) {
                        // entityが読み取ったクラス定義にないので、型名をテーブル名としておく
                        tableName = toSnakeCase(typeId.asSimpleText());
                        return new Tables(new Table(tableName));
                    }

                    tableName = entityDeclaration.jigTypeHeader().jigTypeAttributes().declarationAnnotationInstances().stream()
                            .filter(annotation -> annotation.id().fqn().equals(SPRING_DATA_TABLE))
                            .findFirst()
                            .flatMap(annotation -> annotation.elementTextOf("value")) // TODO nameがaliasなので対応する？
                            .filter(value -> !value.isBlank())
                            // Tableアノテーションがついていない or valueがない
                            // テーブル名が指定されていないので、エンティティの型名をテーブル名としておく
                            .orElseGet(() -> toSnakeCase(typeId.asSimpleText()));
                    return new Tables(new Table(tableName));
                });
    }

    private static boolean isSpringDataRepository(TypeId interfaceId) {
        return interfaceId.fqn().startsWith(SPRING_DATA_REPOSITORY_PREFIX);
    }

    private record SpringDataRepositoryInfo(Optional<TypeId> entityTypeId) {
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

    private static String toSnakeCase(String text) {
        StringBuilder builder = new StringBuilder(text.length() + 4);
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (Character.isUpperCase(current) && i > 0) builder.append('_');
            builder.append(Character.toLowerCase(current));
        }
        return builder.toString();
    }
}
