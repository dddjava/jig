package org.dddjava.jig.domain.model.implementation.bytecode;

import java.util.List;

/**
 * バイトコードのソース一式
 */
public class ByteCodeSources {
    private final List<ByteCodeSource> sources;

    public ByteCodeSources(List<ByteCodeSource> sources) {
        this.sources = sources;
    }

    public List<ByteCodeSource> list() {
        return sources;
    }

    public boolean notFound() {
        return sources.isEmpty();
    }
}
