package org.dddjava.jig.domain.model.sources.javasources;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

/**
 * 読めるテキストソース
 *
 * 先にbytesとして読み込んでおく。このインスタンスでは少なくともIOExceptionは発生しない。
 */
public class ReadableTextSource {

    private final JavaSource javaSource;
    private final byte[] bytes;

    public ReadableTextSource(JavaSource javaSource, byte[] bytes) {
        this.javaSource = javaSource;
        this.bytes = bytes;
    }

    public ByteArrayInputStream toInputStream() {
        return new ByteArrayInputStream(bytes());
    }

    public String fineName() {
        return path().getFileName().toString();
    }

    public Path path() {
        return javaSource.location();
    }

    public byte[] bytes() {
        return bytes;
    }

    @Override
    public String toString() {
        return "ReadableTextSource{path=" + path() + '}';
    }
}
