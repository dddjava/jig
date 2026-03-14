package sample.data;

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
