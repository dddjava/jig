package common;

import org.springframework.data.repository.CrudRepository;

/**
 * 解析対象外の共通ライブラリ的なカスタムリポジトリ基底インターフェース。
 * stubパッケージ外に置くことでJIGの解析対象から除外されている。
 */
public interface MyCrudRepository<T, ID> extends CrudRepository<T, ID> {
}
