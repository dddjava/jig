package stub.infrastructure.datasource.trace;

public class TraceHelper {
    private final TraceMapper traceMapper;

    public TraceHelper(TraceMapper traceMapper) {
        this.traceMapper = traceMapper;
    }

    public boolean save(String key) {
        return traceMapper.binding(key);
    }
}
