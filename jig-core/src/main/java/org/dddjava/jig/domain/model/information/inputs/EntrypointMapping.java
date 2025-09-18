package org.dddjava.jig.domain.model.information.inputs;

public interface EntrypointMapping {
    EntrypointMapping DEFAULT = new EntrypointMapping() {
    };

    default String shortPathText() {
        return fullPathText();
    }

    default String fullPathText() {
        // パスを実装していないEntrypointで呼び出されるパターン
        return "???";
    }
}
