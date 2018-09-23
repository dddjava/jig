package stub.application.service;

import org.dddjava.jig.annotation.Progress;
import org.springframework.stereotype.Service;

/**
 * フィールドを持たないサービス
 */
@Service
@Progress("SimpleServiceのクラスに付けた進捗")
public class SimpleService {

    @Progress("SimpleService#コントローラーから呼ばれるの進捗")
    public void コントローラーから呼ばれる() {
    }

    @Progress("SimpleService#RESTコントローラーから呼ばれるの進捗")
    public void RESTコントローラーから呼ばれる() {
    }

    public void コントローラーから呼ばれない() {
    }
}
