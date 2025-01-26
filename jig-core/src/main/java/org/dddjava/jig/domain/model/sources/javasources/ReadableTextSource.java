package org.dddjava.jig.domain.model.sources.javasources;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

/**
 * 読めるテキストソース
 *
 * 先にbytesとして読み込んでおく。このインスタンスでは少なくともIOExceptionは発生しない。
 */
public class ReadableTextSource {

    private final TextSource textSource;
    private final byte[] bytes;

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

    @Override
    public String toString() {
        return "ReadableTextSource{path=" + path() + '}';
    }
}
