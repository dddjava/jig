package org.dddjava.jig.domain.model.information.jigobject.class_;

public enum TypeCategory {
    /**
     * ユースケース
     *
     * いわゆるService。
     */
    Usecase,

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
     * バウンダリコンポーネント
     *
     * コアドメインではなく、Controller、Service、Repository以外のアプリケーションを動かすために必要なコンポーネント。
     */
    BoundaryComponent,

    /**
     * 判別できなかったもの
     */
    Others;

    public boolean isBoundary() {
        return switch (this) {
            case Usecase, InputAdapter, OutputAdapter, BoundaryComponent -> true;
            default -> false;
        };
    }
}
