package stub.application.service;

import org.springframework.stereotype.Service;

/**
 * 分岐のあるサービス
 */
@Service
public class DecisionService {

    void 分岐のあるメソッド(Object 条件) {
        if (条件 == null) {
            throw new NullPointerException();
        }
    }
}
