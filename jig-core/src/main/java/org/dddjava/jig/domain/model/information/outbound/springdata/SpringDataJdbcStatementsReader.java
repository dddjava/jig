package org.dddjava.jig.domain.model.information.outbound.springdata;

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

    // 推測したカスタム基底リポジトリの蓄積（interfaceId → 宣言元TypeIdリスト）
    private final Map<TypeId, List<TypeId>> inferredBaseRepositories = new LinkedHashMap<>();

    private static final String SPRING_DATA_TABLE = "org.springframework.data.relational.core.mapping.Table";
    private static final String SPRING_DATA_MAPPED_COLLECTION = "org.springframework.data.relational.core.mapping.MappedCollection";
    private static final String SPRING_DATA_QUERY_ANNOTATION = "org.springframework.data.jdbc.repository.query.Query";

    private record SpringDataPersistenceInfo(Collection<PersistenceTarget> targets, Set<TypeId> superTypeIds) {
    }

    /**
     * Spring Data JDBCのRepositoryを抽出して永続化操作対象群を構築する
     *
     * 対象は次の条件をすべて満たす型:
     * 1) interface である
     * 2) 継承先（再帰含む）に {@code org.springframework.data.repository.*} を持つ
     */
    public Collection<PersistenceAccessor> readFrom(JigTypes jigTypes) {
        // 複数回実行されることは想定していないが一応・・・
        inferredBaseRepositories.clear();

        var result = jigTypes.stream()
                .filter(this::isInterface)
                .flatMap(jigType -> resolvePersistenceTargets(jigType, jigTypes).stream()
                        .map(info -> resolvePersistenceAccessors(jigType, info.targets(), info.superTypeIds())))
                .toList();

        inferredBaseRepositories.forEach((interfaceId, declaringTypes) ->
                logger.warn("インターフェース {} がJIGの解析対象に含まれていないため、名前と型引数からSpring Data Repositoryと推測して処理します。" +
                                "正確に解析するには解析対象パスに含めてください。宣言元: {}",
                        interfaceId.fqn(),
                        declaringTypes.stream().map(TypeId::fqn).collect(java.util.stream.Collectors.joining(", "))));
        return result;
    }

    /**
     * SpringDataのエンティティを解決する
     *
     * 対象の型がSpringDataRepositoryを継承したインタフェースであればEntityの型を返す。
     *
     * @param jigType  対象の型
     * @param jigTypes インタフェースの型を解決するための辞書
     */
    private Optional<SpringDataPersistenceInfo> resolvePersistenceTargets(JigType jigType, JigTypes jigTypes) {
        return resolvePersistenceTargets(jigType, jigTypes, new HashSet<>(), new HashSet<>());
    }

    private Optional<SpringDataPersistenceInfo> resolvePersistenceTargets(JigType jigType, JigTypes jigTypes, Set<TypeId> visited, Set<TypeId> superTypeIds) {
        var header = jigType.jigTypeHeader();
        // 再帰しているので一応チェック。普通に作れば型継承の循環はコンパイルエラーになるため、このチェックに出番はない。
        if (!visited.add(header.id())) return Optional.empty();

        // 継承しているインタフェースを確認
        for (JigTypeReference interfaceType : header.interfaceTypeList()) {
            TypeId interfaceId = interfaceType.id();
            if (SpringDataUtil.isSpringDataRepositoryType(interfaceId)) {
                superTypeIds.add(interfaceId);
                List<JigTypeArgument> jigTypeArguments = interfaceType.typeArgumentList();
                if (jigTypeArguments.isEmpty()) {
                    logger.warn("Spring Data Repository {} の型引数が解決できないため、この継承はスキップします。宣言元: {}",
                            interfaceId.fqn(),
                            header.fqn());
                    continue;
                }
                var entityTypeId = jigTypeArguments.getFirst().typeId();
                return Optional.of(new SpringDataPersistenceInfo(extractPersistenceTargets(jigTypes, entityTypeId), superTypeIds));
                // MEMO: SpringDataRepositoryのインタフェースを複数実装している場合も1つ目だけ扱う。複数実装していてもエンティティ型が違うのは想定しない。
            }

            // インタフェースを対象に再帰する。
            Optional<JigType> resolvedInterface = jigTypes.resolveJigType(interfaceId);
            Optional<SpringDataPersistenceInfo> persistenceInfo = resolvedInterface
                    .flatMap(interfaceJigType -> resolvePersistenceTargets(interfaceJigType, jigTypes, visited, superTypeIds));
            if (persistenceInfo.isPresent()) {
                // 継承したインタフェースからPersistenceTargetsの解決に成功した
                return persistenceInfo;
            }

            // JigTypesに見つからないインターフェースが名前と型引数からSpring Data Repositoryと推測できる場合
            if (resolvedInterface.isEmpty()) {
                List<JigTypeArgument> typeArguments = interfaceType.typeArgumentList();
                if (!typeArguments.isEmpty() && interfaceId.fqn().endsWith("Repository")) {
                    inferredBaseRepositories.computeIfAbsent(interfaceId, k -> new ArrayList<>()).add(header.id());
                    superTypeIds.add(interfaceId);
                    var entityTypeId = typeArguments.getFirst().typeId();
                    return Optional.of(new SpringDataPersistenceInfo(extractPersistenceTargets(jigTypes, entityTypeId), superTypeIds));
                }
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
    private PersistenceAccessor resolvePersistenceAccessors(JigType jigType, Collection<PersistenceTarget> defaultPersistenceTargets, Set<TypeId> superTypeIds) {
        TypeId typeId = jigType.jigTypeHeader().id();

        // インタフェースに直接定義されているメソッド
        List<PersistenceAccessorOperation> declaredOperations = jigType.instanceJigMethods().stream()
                .map(jigMethod -> resolvePersistenceAccessor(jigMethod.jigMethodDeclaration(), typeId, defaultPersistenceTargets))
                .flatMap(Optional::stream)
                .toList();

        // インタフェースに直接定義されているメソッド名（オーバーライド済みの判定用）
        // MEMO: オーバーロードが考慮されていない。オーバーロードで @Query を使用している場合に対応する必要がある。
        Set<String> declaredMethodNames = declaredOperations.stream()
                .map(op -> op.id().id())
                .collect(java.util.stream.Collectors.toSet());

        List<PersistenceAccessorOperation> inheritedOperations = SpringDataBaseMethod.stream()
                // メソッド名がかぶるものをオーバーライド済みとして除外（オーバーロードの考慮が必要）
                .filter(baseMethod -> !declaredMethodNames.contains(baseMethod.methodName()))
                .map(baseMethod -> PersistenceAccessorOperation.from(
                        PersistenceAccessorOperationId.fromTypeIdAndName(typeId, baseMethod.methodName()),
                        baseMethod.persistenceOperationType(),
                        defaultPersistenceTargets))
                .toList();

        List<PersistenceAccessorOperation> persistenceAccessorOperations = Stream.concat(
                declaredOperations.stream(), inheritedOperations.stream()).toList();

        return PersistenceAccessor.forSpringDataJdbc(typeId, defaultPersistenceTargets, persistenceAccessorOperations, superTypeIds);
    }

    private Optional<PersistenceAccessorOperation> resolvePersistenceAccessor(JigMethodDeclaration jigMethodDeclaration,
                                                                              TypeId typeId,
                                                                              Collection<PersistenceTarget> persistenceTargets) {
        String methodName = jigMethodDeclaration.name();
        PersistenceAccessorOperationId statementId = PersistenceAccessorOperationId.fromTypeIdAndName(typeId, methodName);

        return resolveQueryFromAnnotation(jigMethodDeclaration)
                .flatMap(annotationQueryString -> {
                    // @Queryがあればそのクエリから推測する
                    Optional<Query> optQuery = Query.from(annotationQueryString);
                    if (optQuery.isEmpty()) {
                        logger.warn("{} の@Queryがうまく処理できませんでした。出力対象から除外されます。value=[{}]",
                                jigMethodDeclaration.fqn(), annotationQueryString);
                    }
                    return optQuery.map(query -> {
                        Optional<PersistenceOperationType> optOperationType = PersistenceOperationType.inferOperationTypeFromQuery(query);
                        if (optOperationType.isEmpty()) {
                            logger.warn("{} の@QueryからCRUDが判別できませんでした。出力対象から除外されます。value=[{}]",
                                    jigMethodDeclaration.fqn(), annotationQueryString);
                        }
                        return optOperationType.map(persistenceOperationType -> PersistenceAccessorOperation.from(statementId, persistenceOperationType, Optional.of(query)));
                    });
                }).orElseGet(() -> {
                    // @Queryがないものはメソッド名でエンティティに対する操作が決まる
                    return SpringDataUtil.inferOperationType(methodName)
                            .map(persistenceOperationType -> PersistenceAccessorOperation.from(statementId, persistenceOperationType, persistenceTargets));
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
    private static Collection<PersistenceTarget> extractPersistenceTargets(JigTypes jigTypes, TypeId entityTypeId) {
        return extractPersistenceTargets(entityTypeId, jigTypes, new HashSet<>()).toList();
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
        // MEMO: コンストラクタ引数などに付与されるケースもありえるが、フィールド探索で主要なケースはカバーできるためフィールドのみ対象とする。
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
