package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.types.TypeId;

public enum TypeCategory {
    /**
     * 入力ポート
     *
     * いわゆるService。
     */
    InputPort,

    /**
     * 入力アダプタ
     *
     * いわゆるController。プレゼンテーションやリクエストをハンドリングするものなど。
     */
    InputAdapter,

    /**
     * 出力アダプタ
     *
     * いわゆるRepository。DB書き込みなどの永続化や、外部サービス呼び出しなど。
     */
    OutputAdapter,

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
        if (jigType.hasAnnotation(TypeId.valueOf("org.springframework.stereotype.Service"))
                || jigType.hasAnnotation(TypeId.from(org.dddjava.jig.annotation.Service.class))) {
            return InputPort;
        }
        if (jigType.hasAnnotation(TypeId.valueOf("org.springframework.stereotype.Controller"))
                || jigType.hasAnnotation(TypeId.valueOf("org.springframework.web.bind.annotation.RestController"))
                || jigType.hasAnnotation(TypeId.valueOf("org.springframework.web.bind.annotation.ControllerAdvice"))
                || jigType.hasAnnotation(TypeId.valueOf("org.dddjava.jig.adapter.HandleDocument"))) {
            return InputAdapter;
        }
        if (jigType.hasAnnotation(TypeId.valueOf("org.springframework.stereotype.Repository"))
                || jigType.hasAnnotation(TypeId.from(org.dddjava.jig.annotation.Repository.class))) {
            return OutputAdapter;
        }
        if (jigType.hasAnnotation(TypeId.valueOf("org.springframework.stereotype.Component"))) {
            return OtherApplicationComponent;
        }

        return Others;
    }

    /**
     * アプリケーションを動作させるためのコンポーネント
     */
    public boolean isApplicationComponent() {
        return switch (this) {
            case InputPort, InputAdapter, OutputAdapter, OtherApplicationComponent -> true;
            default -> false;
        };
    }
}
