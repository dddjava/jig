package sample.data.application;

import sample.data.infrastructure.SampleEntity;

/**
 * サンプルポート
 */
public interface SampleOutputPort {

    /**
     * 検索
     */
    SampleEntity find();

    /**
     * 登録
     */
    void register(String value);
}
