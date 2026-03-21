package sample.data.infrastructure;

import org.springframework.stereotype.Repository;
import sample.data.application.SampleOutboundPort;

/**
 * サンプルアダプタ
 */
@Repository
public class SampleOutboundAdapter implements SampleOutboundPort {

    private final SampleOutboundAccessor accessor;

    public SampleOutboundAdapter(SampleOutboundAccessor accessor) {
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
