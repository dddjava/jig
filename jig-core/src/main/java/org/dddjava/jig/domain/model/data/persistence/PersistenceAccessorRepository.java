package org.dddjava.jig.domain.model.data.persistence;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.*;

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

        // typeIdに合致する型が登録されていない場合の探索。
        // ...ただこれエッジケースだよねぇ。わざわざメソッド内で CrudRepository などにアップキャストして使うとかしないと不要ではないか。
        // なくしてしまっていい気もするが、必要ならキャッシュも考えた方がいいかもしれない。

        // 親クラスのメソッドとして呼び出されている場合
        // interface HogeAccessor extends FugaAccessor {} のとき、呼び出し元が
        // FugaAccessor accessor = ...; accessor.methodCall(); のようなことをすると、MethodCallは FugaAccessor 経由なのでtypeIdもそうなる。
        // PersistenceAccessorとしてはHogeAccessorが登録されているので、HogeAccessorのsuperTypeと突き合わせてPersistenceAccessorを探す。
        List<PersistenceAccessor> candidates = values.stream()
                .filter(ops -> ops.superTypeIds().contains(typeId))
                .toList();
        // PersistenceAccessorとして登録されているのが1つだけならそれを返す
        if (candidates.size() == 1) return Optional.of(candidates.getFirst());

        // 複数候補がある場合は relatedTypes で絞り込む
        // このケースになるのは FugaAccessor を継承しているAccessorが複数ある場合で、
        // 正確に特定しようとするとMethodCallがどのインスタンスに対して行われたかから引くことになり、解決できない場合も生じる。
        // ここではメソッドが使用している型（relatedTypes）から、一意に特定できないかのチャレンジをしている。
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
