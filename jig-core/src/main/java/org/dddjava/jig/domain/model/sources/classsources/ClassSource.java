package org.dddjava.jig.domain.model.sources.classsources;

/**
 * classソース
 */
public record ClassSource(byte[] bytes) {

    @Override
    public String toString() {
        return "ClassSource{bytes.length=" + bytes.length + '}';
    }
}
