package org.dddjava.jig.domain.model.information.types;

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
