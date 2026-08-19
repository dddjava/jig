package org.dddjava.jig.domain.model.sources.mybatis;

/**
 * MyBatisからのSQL読み取りがどこまでできたか
 */
public enum SqlReadStatus {
    成功,
    読み取り失敗あり,
    失敗
}
