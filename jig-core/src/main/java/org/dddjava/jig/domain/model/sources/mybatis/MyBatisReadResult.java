package org.dddjava.jig.domain.model.sources.mybatis;

import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;
import org.dddjava.jig.domain.model.documents.JigIssue;

import java.util.function.Consumer;

public record MyBatisReadResult(PersistenceAccessorRepository persistenceAccessorRepository, Status status) {

    /**
     * MyBatisからのSQL読み取りがどこまでできたか
     */
    public enum Status {
        成功,
        一部失敗,
        失敗
    }

    public MyBatisReadResult(Status status) {
        this(PersistenceAccessorRepository.empty(), status);
    }

    /**
     * 読み取りで生じた問題を通知する。
     */
    public void recordTo(Consumer<JigIssue> recorder) {
        switch (status) {
            case 成功 -> {
                if (persistenceAccessorRepository.isEmpty()) recorder.accept(JigIssue.SQLなし);
            }
            case 一部失敗 -> recorder.accept(JigIssue.SQL読み込み一部失敗);
            case 失敗 -> recorder.accept(JigIssue.SQL読み込み失敗);
        }
    }
}
