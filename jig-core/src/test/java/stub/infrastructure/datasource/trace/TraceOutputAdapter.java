package stub.infrastructure.datasource.trace;

import org.springframework.stereotype.Repository;

@Repository
public class TraceOutputAdapter implements TraceOutputPort {
    private final TraceHelper traceHelper;

    public TraceOutputAdapter(TraceMapper traceMapper) {
        this.traceHelper = new TraceHelper(traceMapper);
    }

    @Override
    public boolean save(String key) {
        return traceHelper.save(key);
    }
}
