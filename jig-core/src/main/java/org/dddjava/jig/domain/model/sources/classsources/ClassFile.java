package org.dddjava.jig.domain.model.sources.classsources;

/**
 * classファイルの中身
 */
public record ClassFile(byte[] bytes) {

    @Override
    public String toString() {
        return "ClassSource{bytes.length=" + bytes.length + '}';
    }
}
