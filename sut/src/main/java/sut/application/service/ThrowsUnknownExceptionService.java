package sut.application.service;

import com.fasterxml.jackson.core.JsonParseException;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.stereotype.Service;

/**
 * メソッドシグネチャにクラスパスに存在しない例外を含むサービス
 */
@Service
public class ThrowsUnknownExceptionService {

    public void jackson() throws JsonParseException {
    }

    public void mybatis() throws PersistenceException {
    }
}
