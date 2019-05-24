package org.dddjava.jig.domain.model.implementation.raw;

import java.io.InputStream;

/**
 * ソースコード
 */
public interface SourceCode {

    SourceFilePath sourceFilePath();

    InputStream toInputStream();
}
