package org.dddjava.jig.domain.model.knowledge.usecases;

/**
 * ユースケースの種類
 */
enum UsecaseCategory {
    ハンドラ,
    その他;

    public static UsecaseCategory resolver(Usecase usecase) {
        return usecase.usingFromController() ? ハンドラ : その他;
    }

    public boolean handler() {
        return this == ハンドラ;
    }
}
