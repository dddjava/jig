package org.dddjava.jig.infrastructure.springdatajdbc;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldHeader;
import org.dddjava.jig.domain.model.data.rdbaccess.*;
import org.dddjava.jig.domain.model.data.types.*;
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
    private static final String SPRING_DATA_MAPPED_COLLECTION = "org.springframework.data.relational.core.mapping.MappedCollection";
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

    private Stream<SqlStatement> extractSqlStatements(ClassDeclaration declaration, TypeId entityTypeId, Map<TypeId, ClassDeclaration> declarationMap) {
        Tables resolvedTables = resolveTablesFromEntityTableAnnotation(entityTypeId, declarationMap);

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
                            return SqlStatement.from(statementId, query, sqlType);
                        }
                        // クエリなしは @Table で記述されているもの
                        return SqlStatement.from(statementId, sqlType, resolvedTables);
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
            if (isSpringDataRepository(interfaceId)) {
                List<JigTypeArgument> jigTypeArguments = interfaceType.typeArgumentList();
                if (jigTypeArguments.isEmpty()) {
                    logger.warn("Spring Data Repository {} の型引数が解決できないため、この継承はスキップします。宣言元: {}",
                            interfaceId.fqn(),
                            header.fqn());
                    continue;
                }
                return Optional.of(new SpringDataRepositoryInfo(jigTypeArguments.getFirst().typeId()));
                // MEMO: SpringDataRepositoryのインタフェースを複数実装している場合は1つ目だけ扱うでいいはず　
            }

            // インタフェースの親インタフェースもたどる。declarationMapになければロードしたクラスにないので諦める。
            ClassDeclaration declaration = declarationMap.get(interfaceId);
            if (declaration != null) {
                Optional<SpringDataRepositoryInfo> repositoryInfo = resolveSpringDataRepositoryInfo(declaration.jigTypeHeader(), declarationMap, visited);
                if (repositoryInfo.isPresent()) return repositoryInfo;
            }
        }
        return Optional.empty();
    }

    private static Tables resolveTablesFromEntityTableAnnotation(TypeId entityTypeId, Map<TypeId, ClassDeclaration> declarationMap) {
        return resolveTablesFromEntity(entityTypeId, declarationMap, new HashSet<>());
    }

    private static Tables resolveTablesFromEntity(TypeId entityTypeId, Map<TypeId, ClassDeclaration> declarationMap, Set<TypeId> visited) {
        if (!visited.add(entityTypeId)) return Tables.nothing();

        return resolveOwnTable(entityTypeId, declarationMap)
                .merge(resolveMappedCollectionTables(entityTypeId, declarationMap, visited));
    }

    private static Tables resolveOwnTable(TypeId entityTypeId, Map<TypeId, ClassDeclaration> declarationMap) {
        ClassDeclaration entityDeclaration = declarationMap.get(entityTypeId);
        String tableName;
        if (entityDeclaration == null) {
            // entityが読み取ったクラス定義にないので、型名をテーブル名としておく
            tableName = toSnakeCase(entityTypeId.asSimpleText());
            return new Tables(new Table(tableName));
        }

        tableName = entityDeclaration.jigTypeHeader().jigTypeAttributes().declarationAnnotationInstances().stream()
                .filter(annotation -> annotation.id().fqn().equals(SPRING_DATA_TABLE))
                .findFirst()
                .flatMap(annotation -> annotation.elementTextOf("value")
                        .or(() -> annotation.elementTextOf("name"))
                )
                .filter(value -> !value.isBlank())
                // Tableアノテーションがついていない or valueがない
                // テーブル名が指定されていないので、エンティティの型名をテーブル名としておく
                .orElseGet(() -> toSnakeCase(entityTypeId.asSimpleText()));
        return new Tables(new Table(tableName));
    }

    private static Tables resolveMappedCollectionTables(TypeId entityTypeId, Map<TypeId, ClassDeclaration> declarationMap, Set<TypeId> visited) {
        ClassDeclaration entityDeclaration = declarationMap.get(entityTypeId);
        if (entityDeclaration == null) return Tables.nothing();

        return entityDeclaration.jigFieldHeaders().stream()
                .flatMap(jigFieldHeader -> resolveMappedCollectionEntityTypeId(jigFieldHeader).stream())
                .map(mappedTypeId -> resolveTablesFromEntity(mappedTypeId, declarationMap, visited))
                .reduce(Tables::merge)
                .orElse(Tables.nothing());
    }

    private static Optional<TypeId> resolveMappedCollectionEntityTypeId(JigFieldHeader jigFieldHeader) {
        boolean hasMappedCollection = jigFieldHeader.declarationAnnotationStream()
                .map(JigAnnotationReference::id)
                .anyMatch(annotationTypeId -> annotationTypeId.fqn().equals(SPRING_DATA_MAPPED_COLLECTION));
        if (!hasMappedCollection) return Optional.empty();

        List<JigTypeArgument> typeArguments = jigFieldHeader.jigTypeReference().typeArgumentList();
        if (typeArguments.isEmpty()) {
            logger.warn("@MappedCollection が指定されたフィールド {} の型引数が解決できないため、このフィールドはスキップします。",
                    jigFieldHeader.id().fqn());
            return Optional.empty();
        }

        // Collection<T> / Map<K, V> のどちらでも末尾を集約対象エンティティとして扱う
        return Optional.of(typeArguments.getLast().typeId());
    }

    private static boolean isSpringDataRepository(TypeId interfaceId) {
        // CrudRepository / PagingAndSortingRepository / Repository などを包含するプレフィックス判定
        return interfaceId.fqn().startsWith(SPRING_DATA_REPOSITORY_PREFIX);
    }

    private record SpringDataRepositoryInfo(TypeId entityTypeId) {
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
