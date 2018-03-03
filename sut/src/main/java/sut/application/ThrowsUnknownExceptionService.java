package sut.application;

import com.fasterxml.jackson.core.JsonParseException;

/**
 * メソッドシグネチャにクラスパスに存在しない例外を含むサービス
 */
public class ThrowsUnknownExceptionService {

    public void jackson() throws JsonParseException {
    }


    // リフレクションで取得しようとすると、使用側のクラスパスにmybatisがないと失敗する
    // public void mybatis() throws PersistenceException {
    // }
}
