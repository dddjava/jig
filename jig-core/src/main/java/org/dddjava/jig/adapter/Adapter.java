package org.dddjava.jig.adapter;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;

import java.nio.file.Path;
import java.util.List;

/**
 * Adapterのインタフェース。
 * このインタフェースを実装したクラスの {@link HandleDocument} メソッドを呼び出してwriterに引き渡す。
 */
public interface Adapter {

    default List<Path> write(Object result, JigDocument jigDocument) {
        throw new UnsupportedOperationException();
    }
}
