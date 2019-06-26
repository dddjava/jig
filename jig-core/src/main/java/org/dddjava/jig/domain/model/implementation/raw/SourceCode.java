package org.dddjava.jig.domain.model.implementation.raw;

import org.dddjava.jig.domain.model.implementation.raw.sourcepath.SourceFilePath;

import java.io.InputStream;

/**
 * ソースコード
 */
public interface SourceCode {

    SourceFilePath sourceFilePath();

    InputStream toInputStream();
}
