package org.dddjava.jig.infrastructure.springdatajdbc;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldHeader;
import org.dddjava.jig.domain.model.data.persistence.*;
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

        Collection<PersistenceOperations> statements = classDeclarations.stream()
                .filter(this::isInterface)
                .flatMap(declaration -> resolveSpringDataRepositoryInfo(declaration.jigTypeHeader(), declarationMap, new HashSet<>())
                        .stream()
                        .map(repositoryInfo -> extractSqlStatements(declaration, repositoryInfo.entityTypeId(), declarationMap)))
                .toList();

        return SqlStatements.from(statements);
    }

    private PersistenceOperations extractSqlStatements(ClassDeclaration declaration, TypeId entityTypeId, Map<TypeId, ClassDeclaration> declarationMap) {
        Tables resolvedTables = resolveTablesFromEntityTableAnnotation(entityTypeId, declarationMap);

        TypeId typeId = declaration.jigTypeHeader().id();
        List<PersistenceOperation> persistenceOperations = declaration.jigMethodDeclarations().stream()
                .map(jigMethodDeclaration -> {
                    String methodName = jigMethodDeclaration.header().name();
                    Query query = resolveQueryFromAnnotation(jigMethodDeclaration);
                    Optional<SqlType> inferredSqlType = query.supported()
                            ? SqlType.inferSqlTypeFromQuery(query)
                            : inferSqlType(methodName);
                    return inferredSqlType.map(sqlType -> {
                        PersistenceOperationId statementId = PersistenceOperationId.fromTypeIdAndName(typeId, methodName);
                        // クエリがあればクエリを優先
                        if (query.supported()) {
                            return PersistenceOperation.from(statementId, query, sqlType);
                        }
                        // クエリなしは @Table で記述されているもの
                        return PersistenceOperation.from(statementId, sqlType, resolvedTables);
                    });
                })
                .flatMap(Optional::stream)
                .toList();

        return new PersistenceOperations(typeId, persistenceOperations);
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
        return new Tables(resolveTablesFromEntity(entityTypeId, declarationMap, new HashSet<>()).toList());
    }

    private static Stream<Table> resolveTablesFromEntity(TypeId entityTypeId, Map<TypeId, ClassDeclaration> declarationMap, Set<TypeId> visited) {
        if (!visited.add(entityTypeId)) return Stream.empty();

        ClassDeclaration entityDeclaration = declarationMap.get(entityTypeId);
        if (entityDeclaration == null) {
            // entityが読み取ったクラス定義にないので、型名をテーブル名としておく
            String tableName = toSnakeCase(entityTypeId.asSimpleText());
            logger.warn("Entity {} のクラス情報が読み取り対象に含まれていません。テーブル名を仮に {} として続行します。", entityTypeId, tableName);
            return Stream.of(new Table(tableName));
        }

        return Stream.concat(resolveOwnTable(entityDeclaration, entityTypeId),
                resolveMappedCollectionTables(entityDeclaration, declarationMap, visited));
    }

    private static Stream<Table> resolveOwnTable(ClassDeclaration entityDeclaration, TypeId entityTypeId) {
        String tableName = entityDeclaration.jigTypeHeader().jigTypeAttributes().declarationAnnotationInstances().stream()
                .filter(annotation -> annotation.id().fqn().equals(SPRING_DATA_TABLE))
                .findFirst()
                .flatMap(annotation -> annotation.elementTextOf("value")
                        .or(() -> annotation.elementTextOf("name"))
                )
                .filter(value -> !value.isBlank())
                // Tableアノテーションがついていない or valueがない
                // テーブル名が指定されていないので、エンティティの型名をテーブル名としておく
                // 本来は属性なしはクラス名から命名戦略に基づいてテーブル名は決定される
                // https://spring.pleiades.io/spring-data/relational/reference/jdbc/mapping.html#entity-persistence.naming-strategy
                .orElseGet(() -> {
                    String name = toSnakeCase(entityTypeId.asSimpleText());
                    logger.info("Entity {} に@Tableが付与されていない or valueが指定されていません。テーブル名を仮に {} として続行します。", entityTypeId, name);
                    return name;
                });
        return Stream.of(new Table(tableName));
    }

    private static Stream<Table> resolveMappedCollectionTables(ClassDeclaration entityDeclaration, Map<TypeId, ClassDeclaration> declarationMap, Set<TypeId> visited) {
        return entityDeclaration.jigFieldHeaders().stream()
                .flatMap(jigFieldHeader -> resolveMappedCollectionEntityTypeId(jigFieldHeader).stream())
                // 再帰的に拾ってくる
                .flatMap(mappedTypeId -> resolveTablesFromEntity(mappedTypeId, declarationMap, visited));
    }

    /**
     * MappedCollectionが付与されているフィールドのエンティティ型を抽出する
     * フィールドは総称型コンテナなので型引数からエンティティ型を解決する。
     * ドキュメント的にはList,Set,Mapのいずれか。
     *
     * https://spring.pleiades.io/spring-data/relational/reference/api/java/org/springframework/data/relational/core/mapping/MappedCollection.html
     */
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
