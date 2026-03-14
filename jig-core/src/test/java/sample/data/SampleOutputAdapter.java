package sample.data;

import org.springframework.stereotype.Repository;

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
