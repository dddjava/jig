package org.dddjava.jig.domain.model.sources.mybatis;

import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;
import org.dddjava.jig.domain.model.sources.ReadStatus;

import java.util.function.Consumer;

public record MyBatisReadResult(PersistenceAccessorRepository persistenceAccessorRepository, SqlReadStatus sqlReadStatus) {

    public MyBatisReadResult(SqlReadStatus sqlReadStatus) {
        this(PersistenceAccessorRepository.empty(), sqlReadStatus);
    }

    /**
     * 読み取り結果として記録すべき事象を通知する。
     */
    public void recordTo(Consumer<ReadStatus> recorder) {
        switch (status()) {
            case 成功 -> { } // 記録するものなし
            case SQLなし -> recorder.accept(ReadStatus.SQLなし);
            case 読み取り失敗あり -> recorder.accept(ReadStatus.SQL読み込み一部失敗);
            case 失敗 -> recorder.accept(ReadStatus.SQL読み込み失敗);
        }
    }

    private SqlReadStatus status() {
        if (sqlReadStatus == SqlReadStatus.成功 && persistenceAccessorRepository.isEmpty()) {
            return SqlReadStatus.SQLなし;
        }
        return sqlReadStatus;
    }
}
