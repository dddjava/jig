package stub.application.service;

import org.springframework.stereotype.Service;

/**
 * フィールドを持たないサービス
 */
@Service
public class SimpleService {

    public void コントローラーから呼ばれる() {
    }

    public void RESTコントローラーから呼ばれる() {
    }

    public void コントローラーから呼ばれない() {
    }
}
