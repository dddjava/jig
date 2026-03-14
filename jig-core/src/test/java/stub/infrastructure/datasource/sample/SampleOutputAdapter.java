package stub.infrastructure.datasource.sample;

import org.springframework.stereotype.Repository;

@Repository
public class SampleOutputAdapter implements SampleOutputPort {
    @Override
    public void register(String value) {
        // サンプル用 - 永続化操作なし
    }
}
