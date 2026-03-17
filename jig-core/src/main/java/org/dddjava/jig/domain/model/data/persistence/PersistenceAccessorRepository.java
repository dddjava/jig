package org.dddjava.jig.domain.model.data.persistence;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 永続化操作リポジトリ
 *
 * @param values 永続化操作群を保持する可変コレクション
 */
public record PersistenceAccessorRepository(Collection<PersistenceAccessor> values) {

    public static PersistenceAccessorRepository empty() {
        return new PersistenceAccessorRepository(new ArrayList<>());
    }

    public static PersistenceAccessorRepository from(Collection<PersistenceAccessor> statements) {
        return new PersistenceAccessorRepository(new ArrayList<>(statements));
    }

    public Optional<PersistenceAccessor> findByTypeId(TypeId typeId) {
        return findByTypeId(typeId, Set.of());
    }

    /**
     * @param typeId       検索対象の型ID
     * @param relatedTypes Spring Data基底型経由で複数候補がある場合の絞り込み型セット
     */
    public Optional<PersistenceAccessor> findByTypeId(TypeId typeId, Set<TypeId> relatedTypes) {
        // 直接ルックアップ
        Optional<PersistenceAccessor> direct = values.stream()
                .filter(ops -> ops.typeId().equals(typeId))
                .findAny();
        if (direct.isPresent()) return direct;

        // Spring Data基底型を経由した解決
        List<PersistenceAccessor> candidates = values.stream()
                .filter(ops -> ops.springDataBaseTypes().contains(typeId))
                .toList();
        if (candidates.size() == 1) return Optional.of(candidates.getFirst());

        // 複数候補がある場合は relatedTypes で絞り込む
        if (!candidates.isEmpty() && !relatedTypes.isEmpty()) {
            List<PersistenceAccessor> filtered = candidates.stream()
                    .filter(ops -> relatedTypes.contains(ops.typeId()))
                    .toList();
            if (filtered.size() == 1) return Optional.of(filtered.getFirst());
        }

        return Optional.empty();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public void register(Collection<PersistenceAccessor> springDataJdbcStatements) {
        // ここで追加するためにvaluesは可変コレクションである必要がある
        values.addAll(springDataJdbcStatements);
    }
}
