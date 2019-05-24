package org.dddjava.jig.domain.model.implementation.raw;

import java.util.List;

/**
 * ソースコード一覧
 */
public interface SourceCodes<T extends SourceCode> {

    List<T> list();
}
