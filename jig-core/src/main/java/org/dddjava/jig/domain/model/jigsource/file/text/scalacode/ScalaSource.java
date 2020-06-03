package org.dddjava.jig.domain.model.jigsource.file.text.scalacode;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * .scalaソース
 */
public class ScalaSource {

    ScalaSourceFile scalaSourceFile;
    byte[] value;

    public ScalaSource(ScalaSourceFile scalaSourceFile, byte[] value) {
        this.scalaSourceFile = scalaSourceFile;
        this.value = value;
    }

    public ScalaSourceFile sourceFilePath() {
        return scalaSourceFile;
    }

    public InputStream toInputStream() {
        return new ByteArrayInputStream(value);
    }

    @Override
    public String toString() {
        return "ScalaSource[" + scalaSourceFile + "]";
    }
}
