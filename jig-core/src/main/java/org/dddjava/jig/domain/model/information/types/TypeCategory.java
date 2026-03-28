package org.dddjava.jig.domain.model.information.types;

public enum TypeCategory {
    /**
     * 入力ポート
     *
     * いわゆるService。
     */
    InboundPort,

    /**
     * 入力アダプタ
     *
     * いわゆるController。プレゼンテーションやリクエストをハンドリングするものなど。
     */
    InboundAdapter,

    /**
     * 出力アダプタ
     *
     * いわゆるRepository。DB書き込みなどの永続化や、外部サービス呼び出しなど。
     */
    OutboundAdapter,

    /**
     * その他のコンポーネント
     *
     * コアドメインではなく、Controller、Service、Repository以外のアプリケーションを動かすために必要なコンポーネント。
     */
    OtherApplicationComponent,

    /**
     * 判別できなかったもの
     */
    Others;

    public static TypeCategory from(JigType jigType) {
        // TODO カスタムアノテーション対応 https://github.com/dddjava/jig/issues/343
        if (jigType.hasAnnotation(SpringAnnotations.SERVICE)
                || jigType.hasAnnotation(JigAnnotations.SERVICE)) {
            return InboundPort;
        }
        if (jigType.hasAnnotation(SpringAnnotations.CONTROLLER)
                || jigType.hasAnnotation(SpringAnnotations.REST_CONTROLLER)
                || jigType.hasAnnotation(SpringAnnotations.CONTROLLER_ADVICE)
                || jigType.hasAnnotation(JigAnnotations.HANDLE_DOCUMENT)) {
            return InboundAdapter;
        }
        if (jigType.hasAnnotation(SpringAnnotations.REPOSITORY)
                || jigType.hasAnnotation(JigAnnotations.REPOSITORY)) {
            return OutboundAdapter;
        }
        if (jigType.hasAnnotation(SpringAnnotations.COMPONENT)) {
            return OtherApplicationComponent;
        }

        return Others;
    }

    /**
     * アプリケーションを動作させるためのコンポーネント
     */
    public boolean isApplicationComponent() {
        return switch (this) {
            case InboundPort, InboundAdapter, OutboundAdapter, OtherApplicationComponent -> true;
            default -> false;
        };
    }
}
