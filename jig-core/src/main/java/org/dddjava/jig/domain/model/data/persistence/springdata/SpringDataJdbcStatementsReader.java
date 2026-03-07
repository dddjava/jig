package org.dddjava.jig.domain.model.data.persistence.springdata;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldHeader;
import org.dddjava.jig.domain.model.data.persistence.*;
import org.dddjava.jig.domain.model.data.types.*;
import org.dddjava.jig.domain.model.information.members.JigField;
import org.dddjava.jig.domain.model.information.members.JigMethodDeclaration;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

public class SpringDataJdbcStatementsReader {
    private static final Logger logger = LoggerFactory.getLogger(SpringDataJdbcStatementsReader.class);

    private static final String SPRING_DATA_REPOSITORY_PREFIX = "org.springframework.data.repository.";
    private static final String SPRING_DATA_TABLE = "org.springframework.data.relational.core.mapping.Table";
    private static final String SPRING_DATA_MAPPED_COLLECTION = "org.springframework.data.relational.core.mapping.MappedCollection";
    private static final String SPRING_DATA_QUERY_ANNOTATION = "org.springframework.data.jdbc.repository.query.Query";

    /**
     * ASMで読み取ったクラス情報から、Spring Data JDBCのRepositoryを抽出して永続化操作対象群を構築する
     *
     * 対象は次の条件をすべて満たす型:
     * 1) interface である
     * 2) 継承先（再帰含む）に {@code org.springframework.data.repository.*} を持つ
     */
    public Collection<PersistenceOperations> readFrom(JigTypes jigTypes) {

        return jigTypes.stream()
                .filter(this::isInterface)
                .flatMap(jigType -> resolvePersistenceTargets(jigType, jigTypes).stream()
                        .map(persistenceTargets -> resolvePersistenceOperations(jigType, persistenceTargets)))
                .toList();
    }

    /**
     * SpringDataのエンティティを解決する
     *
     * 対象の型がSpringDataRepositoryを継承したインタフェースであればEntityの型を返す。
     *
     * @param jigType  対象の型
     * @param jigTypes インタフェースの型を解決するための辞書
     */
    private Optional<PersistenceTargets> resolvePersistenceTargets(JigType jigType, JigTypes jigTypes) {
        return resolvePersistenceTargets(jigType, jigTypes, new HashSet<>());
    }

    private Optional<PersistenceTargets> resolvePersistenceTargets(JigType jigType, JigTypes jigTypes, Set<TypeId> visited) {
        var header = jigType.jigTypeHeader();
        // 再帰しているので一応チェック。普通に作れば型継承の循環はコンパイルエラーになるため、このチェックに出番はない。
        if (!visited.add(header.id())) return Optional.empty();

        // 継承しているインタフェースを確認
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
                var entityTypeId = jigTypeArguments.getFirst().typeId();
                return Optional.of(extractPersistenceTargets(jigTypes, entityTypeId));
                // MEMO: SpringDataRepositoryのインタフェースを複数実装している場合も1つ目だけ扱う。複数実装していてもエンティティ型が違うのは想定しない。
            }

            // インタフェースを対象に再帰する。
            Optional<PersistenceTargets> persistenceTargets = jigTypes.resolveJigType(interfaceId)
                    .flatMap(interfaceJigType -> resolvePersistenceTargets(interfaceJigType, jigTypes, visited));
            if (persistenceTargets.isPresent()) {
                // 継承したインタフェースからPersistenceTargetsの解決に成功した
                return persistenceTargets;
            }

            // インタフェースがJigTypesにない場合や継承階層を辿ってもemptyな場合
            // 複数インタフェースを実装している場合に次を処理する
        }

        // 継承先にSpringDataを含まない
        return Optional.empty();
    }

    /**
     * SpringDataのRepositoryに対する永続化操作群を作成する
     */
    private PersistenceOperations resolvePersistenceOperations(JigType jigType, PersistenceTargets defaultPersistenceTargets) {
        TypeId typeId = jigType.jigTypeHeader().id();

        List<PersistenceOperation> persistenceOperations = jigType.instanceJigMethods().stream()
                .map(jigMethod -> resolvePersistenceOperation(jigMethod.jigMethodDeclaration(), typeId, defaultPersistenceTargets))
                .flatMap(Optional::stream)
                .toList();

        return PersistenceOperations.forSpringDataJdbc(typeId, defaultPersistenceTargets, persistenceOperations);
    }

    private Optional<PersistenceOperation> resolvePersistenceOperation(JigMethodDeclaration jigMethodDeclaration,
                                                                       TypeId typeId,
                                                                       PersistenceTargets persistenceTargets) {
        String methodName = jigMethodDeclaration.name();
        PersistenceOperationId statementId = PersistenceOperationId.fromTypeIdAndName(typeId, methodName);

        return resolveQueryFromAnnotation(jigMethodDeclaration)
                .flatMap(annotationQueryString -> {
                    // @Queryがあればそのクエリから推測する
                    Optional<Query> optQuery = Query.fromSafety(annotationQueryString);
                    if (optQuery.isEmpty()) {
                        logger.warn("{} の@Queryがうまく処理できませんでした。出力対象から除外されます。value=[{}]",
                                jigMethodDeclaration.fqn(), annotationQueryString);
                    }
                    return optQuery.map(query -> {
                        Optional<SqlType> optSqlType = SqlType.inferSqlTypeFromQuery(query);
                        if (optSqlType.isEmpty()) {
                            logger.warn("{} の@QueryからCRUDが判別できませんでした。出力対象から除外されます。value=[{}]",
                                    jigMethodDeclaration.fqn(), annotationQueryString);
                        }
                        return optSqlType.map(sqlType -> PersistenceOperation.from(statementId, query, sqlType));
                    });
                }).orElseGet(() -> {
                    // @Queryがないものはメソッド名でエンティティに対する操作が決まる
                    return SpringDataUtil.inferSqlType(methodName)
                            .map(sqlType -> PersistenceOperation.from(statementId, sqlType, persistenceTargets));
                });
    }

    /**
     * SpringDataJDBCのQueryアノテーションからクエリ文字列を取得する
     */
    private static Optional<String> resolveQueryFromAnnotation(JigMethodDeclaration methodDeclaration) {
        return methodDeclaration.header().declarationAnnotationStream()
                .filter(annotation -> annotation.id().fqn().equals(SPRING_DATA_QUERY_ANNOTATION))
                .findAny()
                .flatMap(annotation -> annotation.elementTextOf("value"));
    }

    private boolean isInterface(JigType jigType) {
        JigTypeHeader header = jigType.jigTypeHeader();
        return header.javaTypeDeclarationKind() == JavaTypeDeclarationKind.INTERFACE;
    }

    /**
     * Entityに対する永続化操作対象群を抽出する
     *
     * Tableアノテーションから永続化操作対象（テーブル名）を取得する。
     * シンプルなエンティティでは1件だが、MappedCollectionによる複数テーブルの場合に複数件となる。
     */
    private static PersistenceTargets extractPersistenceTargets(JigTypes jigTypes, TypeId entityTypeId) {
        return new PersistenceTargets(extractPersistenceTargets(entityTypeId, jigTypes, new HashSet<>()).toList());
    }

    private static Stream<PersistenceTarget> extractPersistenceTargets(TypeId entityTypeId, JigTypes jigTypes, Set<TypeId> visited) {
        // 循環参照になっている場合（自分を参照する結合とかだとありえる？）
        if (!visited.add(entityTypeId)) return Stream.empty();

        return jigTypes.resolveJigType(entityTypeId)
                .map(entityType -> Stream.concat(resolveOwnTable(entityType, entityTypeId),
                        resolveMappedCollectionTables(entityType, jigTypes, visited)))
                .orElseGet(() -> {
                    // entityが読み取ったクラス定義にないので、型名をテーブル名としておく
                    String tableName = toSnakeCase(entityTypeId.asSimpleText());
                    logger.warn("Entity {} のクラス情報が読み取り対象に含まれていません。テーブル名を仮に {} として続行します。", entityTypeId, tableName);
                    return Stream.of(new PersistenceTarget(tableName));
                });
    }

    private static Stream<PersistenceTarget> resolveOwnTable(JigType jigType, TypeId entityTypeId) {
        String tableName = jigType.jigTypeHeader().jigTypeAttributes().declarationAnnotationInstances().stream()
                .filter(annotation -> annotation.id().fqn().equals(SPRING_DATA_TABLE))
                .findAny()
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
        return Stream.of(new PersistenceTarget(tableName));
    }

    private static Stream<PersistenceTarget> resolveMappedCollectionTables(JigType jigType, JigTypes jigTypes, Set<TypeId> visited) {
        // TODO フィールドだけでよかったっけ？
        return jigType.jigTypeMembers().instanceFields().stream()
                .map(JigField::jigFieldHeader)
                .flatMap(jigFieldHeader -> resolveMappedCollectionEntityTypeId(jigFieldHeader).stream())
                // 再帰的に拾ってくる
                .flatMap(mappedTypeId -> extractPersistenceTargets(mappedTypeId, jigTypes, visited));
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
