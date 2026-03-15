package sample.data.infrastructure;

import org.springframework.stereotype.Repository;
import sample.data.application.SampleOutputPort;

/**
 * サンプルアダプタ
 */
@Repository
public class SampleOutputAdapter implements SampleOutputPort {

    private final SampleOutputAccessor accessor;

    public SampleOutputAdapter(SampleOutputAccessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public SampleEntity find() {
        return accessor.findById(1L);
    }

    public void register(String value) {
        accessor.save(new SampleEntity());
    }
}
