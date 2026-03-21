package org.dddjava.jig.domain.model.information.outbound;

import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.stream.Stream;

/**
 * 出力ポート
 *
 * 通常は操作のみを定義しているRepositoryインタフェース。
 * ポートはアダプタのことは知らない。
 */
public record OutboundPort(JigType jigType) {
    public OutboundPort {
        // 「インタフェースである」を条件としたいが、
        // 小さいアプリケーションではポート兼アダプタはありえるので許容。
    }

    /**
     * ポートの操作を舐めるストリームを取得する
     *
     * すべてのインスタンスメソッドを操作として返しているが、それでいいのか？という疑問はある。
     */
    public Stream<OutboundPortOperation> operationStream() {
        return jigType().instanceJigMethodStream()
                .map(OutboundPortOperation::new);
    }
}
