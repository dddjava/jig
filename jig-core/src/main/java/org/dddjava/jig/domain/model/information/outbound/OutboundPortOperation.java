package org.dddjava.jig.domain.model.information.outbound;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.information.members.JigMethod;

/**
 * 出力ポートの操作
 */
public record OutboundPortOperation(JigMethod jigMethod) {

    /**
     * 操作に対する実行を判定する
     */
    public boolean matches(JigMethod outboundAdapterExecutionJigMethodCandidate) {
        var candidateJigMethodId = outboundAdapterExecutionJigMethodCandidate.jigMethodId();
        var jigMethodId = jigMethod.jigMethodId();

        // メソッド名と引数の型が一致すればマッチ
        // TODO ジェネリクス対応できてなさそう
        return jigMethodId.name().equals(outboundAdapterExecutionJigMethodCandidate.name())
                && jigMethodId.tuple().parameterTypeNameList().equals(candidateJigMethodId.tuple().parameterTypeNameList());
    }

    public JigMethodId jigMethodId() {
        return jigMethod.jigMethodId();
    }
}
