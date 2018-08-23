package stub.application.service;

import org.dddjava.jig.annotation.incubation.Progress;
import org.springframework.stereotype.Service;

/**
 * 分岐のあるサービス
 */
@Service
public class DecisionService {

    @Progress("DecisionService#分岐のあるメソッドの進捗")
    void 分岐のあるメソッド(Object 条件) {
        if (条件 == null) {
            throw new NullPointerException();
        }
    }
}
