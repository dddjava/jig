package stub.infrastructure.datasource;

import org.springframework.stereotype.Repository;

@Repository
public class DecisionDatasource {

    void 分岐のあるメソッド(Object 条件) {
        if (条件 == null) {
            throw new NullPointerException();
        }
    }
}
