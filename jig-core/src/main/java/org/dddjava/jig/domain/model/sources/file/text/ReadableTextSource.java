package org.dddjava.jig.domain.model.sources.file.text;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

/**
 * 読めるテキストソース
 *
 * 先にbytesとして読み込んでおく。このインスタンスでは少なくともIOExceptionは発生しない。
 */
public class ReadableTextSource {

    private TextSource textSource;
    private byte[] bytes;

    public ReadableTextSource(TextSource textSource, byte[] bytes) {
        this.textSource = textSource;
        this.bytes = bytes;
    }

    public ByteArrayInputStream toInputStream() {
        return new ByteArrayInputStream(bytes());
    }

    public String fineName() {
        return path().getFileName().toString();
    }

    public Path path() {
        return textSource.location();
    }

    public byte[] bytes() {
        return bytes;
    }
}
