package org.dddjava.jig.domain.model.data.persistence;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistenceOperationTypeTest {

    private static final PersistenceAccessorOperationId DUMMY_ID =
            PersistenceAccessorOperationId.fromTypeIdAndName(TypeId.valueOf("example.Repo"), "method");

    private Map<String, PersistenceOperationType> extractTypeMap(String sql, PersistenceOperationType operationType) {
        Query query = Query.from(sql);
        PersistenceOperations targets = operationType.extractTable(query, DUMMY_ID);
        return targets.persistenceTargets().stream()
                .collect(Collectors.toMap(
                        persistenceOperation -> persistenceOperation.persistenceTarget().name(),
                        persistenceOperation -> persistenceOperation.operationType(),
                        (a, b) -> a));
    }

    @Test
    void UPDATEでサブクエリ内FROMのテーブルをSELECTとして抽出する() {
        String sql = "UPDATE t1 SET col = 1 WHERE id IN (SELECT id FROM t2)";
        Map<String, PersistenceOperationType> result = extractTypeMap(sql, PersistenceOperationType.UPDATE);

        assertEquals(PersistenceOperationType.UPDATE, result.get("t1"));
        assertEquals(PersistenceOperationType.SELECT, result.get("t2"));
    }

    @Test
    void ネストしたサブクエリのFROMテーブルを全て抽出する() {
        String sql = "UPDATE t1 SET col = 1 WHERE id IN (SELECT id FROM t2 WHERE id = (SELECT MAX(id) FROM t3))";
        Map<String, PersistenceOperationType> result = extractTypeMap(sql, PersistenceOperationType.UPDATE);

        assertEquals(PersistenceOperationType.UPDATE, result.get("t1"));
        assertEquals(PersistenceOperationType.SELECT, result.get("t2"));
        assertEquals(PersistenceOperationType.SELECT, result.get("t3"));
    }

    @Test
    void DELETEでサブクエリ内FROMのテーブルをSELECTとして抽出する() {
        String sql = "DELETE FROM t1 WHERE id IN (SELECT id FROM t2)";
        Map<String, PersistenceOperationType> result = extractTypeMap(sql, PersistenceOperationType.DELETE);

        assertEquals(PersistenceOperationType.DELETE, result.get("t1"));
        assertEquals(PersistenceOperationType.SELECT, result.get("t2"));
    }

    @Test
    void サブクエリなしのUPDATEは既存動作のまま() {
        String sql = "UPDATE t1 SET col = 1 WHERE id = 1";
        Map<String, PersistenceOperationType> result = extractTypeMap(sql, PersistenceOperationType.UPDATE);

        assertEquals(1, result.size());
        assertEquals(PersistenceOperationType.UPDATE, result.get("t1"));
    }

    @Test
    void コロン付きパラメータのfromをテーブル名として誤検出しない() {
        // Spring Data JDBCの :from パラメータが FROM キーワードと誤認識されないこと
        String sql = "SELECT * FROM hoge WHERE id IN (:idList) AND date BETWEEN :from AND :to";
        Query query = Query.from(sql);
        PersistenceOperations targets = PersistenceOperationType.SELECT.extractTable(query, DUMMY_ID);

        assertEquals("[hoge]", targets.asText());
    }

    @Test
    void nextvalはサブクエリパターンの影響を受けない() {
        String sql = "SELECT nextval('seq_name')";
        Query query = Query.from(sql);
        PersistenceOperations targets = PersistenceOperationType.SELECT.extractTable(query, DUMMY_ID);

        assertEquals("[nextval('seq_name')]", targets.asText());
    }

    @Test
    void 算術式内のサブクエリからテーブル名を正しく抽出する() {
        // COUNT(*) のような関数呼び出しを含むサブクエリ
        String sql = "SELECT 20 - (SELECT COUNT(*) FROM hoge) FROM dual";
        Map<String, PersistenceOperationType> result = extractTypeMap(sql, PersistenceOperationType.SELECT);

        assertEquals(PersistenceOperationType.SELECT, result.get("hoge"));
        assertEquals(PersistenceOperationType.SELECT, result.get("dual"));
        // hoge) のようなゴミが入っていないこと
        assertEquals(2, result.size());
    }

    @Test
    void EXISTSサブクエリからテーブル名を正しく抽出する() {
        // EXISTS(SELECT ... FROM table FOR UPDATE OF "table") の形式
        String sql = "SELECT exists(SELECT 1 FROM hoge WHERE journey_id = :xxxxId FOR UPDATE OF \"hoge\")";
        Query query = Query.from(sql);
        PersistenceOperations targets = PersistenceOperationType.SELECT.extractTable(query, DUMMY_ID);

        assertEquals("[hoge]", targets.asText());
    }

    @Test
    void テーブル名の引用符は除去される() {
        String sql = "SELECT * FROM \"hoge\".\"fuga\" WHERE id = 1";
        Query query = Query.from(sql);
        PersistenceOperations targets = PersistenceOperationType.SELECT.extractTable(query, DUMMY_ID);

        assertEquals("[hoge.fuga]", targets.asText());

        String mysqlSql = "SELECT * FROM `db`.`table` WHERE id = 1";
        Query query2 = Query.from(mysqlSql);
        PersistenceOperations targets2 = PersistenceOperationType.SELECT.extractTable(query2, DUMMY_ID);

        assertEquals("[db.table]", targets2.asText());
    }
}
