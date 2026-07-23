package org.dddjava.jig.domain.model.information.outbound.mybatis.ut;

/**
 * OutboundAdapter が直接 Mapper を呼ぶのではなく、間に挟んだ普通のクラス経由で呼ぶケース。
 * OutboundAdapterExecution のメソッド呼び出し追跡が、Adapter/Repository以外の
 * 中間クラスを挟んでも辿れることを確認する。
 */
public class TraceHelper {
    private final TraceMapper traceMapper;

    public TraceHelper(TraceMapper traceMapper) {
        this.traceMapper = traceMapper;
    }

    public boolean save(String key) {
        return traceMapper.binding(key);
    }
}
