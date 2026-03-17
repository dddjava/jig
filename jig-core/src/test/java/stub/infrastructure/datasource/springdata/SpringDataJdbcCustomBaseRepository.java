package stub.infrastructure.datasource.springdata;

import common.MyCrudRepository;
import org.springframework.stereotype.Repository;

/**
 * 共通ライブラリの MyCrudRepository を経由して CrudRepository を継承するケース。
 * MyCrudRepository は JIG の解析対象外であるため、名前ベースのヒューリスティックで処理される。
 */
@Repository
public interface SpringDataJdbcCustomBaseRepository extends MyCrudRepository<SpringDataJdbcOrder, Long> {
}
