package org.dddjava.jig.domain.model.sources.mybatis;

import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;
import org.dddjava.jig.domain.model.documents.JigIssue;

import java.util.function.Consumer;

public record MyBatisReadResult(PersistenceAccessorRepository persistenceAccessorRepository, SqlReadStatus sqlReadStatus) {

    public MyBatisReadResult(SqlReadStatus sqlReadStatus) {
        this(PersistenceAccessorRepository.empty(), sqlReadStatus);
    }

    /**
     * 読み取りで生じた問題を通知する。
     */
    public void recordTo(Consumer<JigIssue> recorder) {
        switch (status()) {
            case 成功 -> { } // 記録するものなし
            case SQLなし -> recorder.accept(JigIssue.SQLなし);
            case 読み取り失敗あり -> recorder.accept(JigIssue.SQL読み込み一部失敗);
            case 失敗 -> recorder.accept(JigIssue.SQL読み込み失敗);
        }
    }

    private SqlReadStatus status() {
        if (sqlReadStatus == SqlReadStatus.成功 && persistenceAccessorRepository.isEmpty()) {
            return SqlReadStatus.SQLなし;
        }
        return sqlReadStatus;
    }
}
