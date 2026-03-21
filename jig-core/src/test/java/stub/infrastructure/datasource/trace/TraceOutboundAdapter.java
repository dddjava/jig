package stub.infrastructure.datasource.trace;

import org.springframework.stereotype.Repository;

@Repository
public class TraceOutboundAdapter implements TraceOutboundPort {
    private final TraceHelper traceHelper;

    public TraceOutboundAdapter(TraceMapper traceMapper) {
        this.traceHelper = new TraceHelper(traceMapper);
    }

    @Override
    public boolean save(String key) {
        return traceHelper.save(key);
    }
}
