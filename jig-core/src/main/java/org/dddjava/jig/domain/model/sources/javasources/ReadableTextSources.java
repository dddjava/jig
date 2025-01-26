package org.dddjava.jig.domain.model.sources.javasources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadableTextSources {
    static Logger logger = LoggerFactory.getLogger(ReadableTextSources.class);

    private final List<JavaSource> javaSources;

    public ReadableTextSources(List<JavaSource> javaSources) {
        this.javaSources = javaSources;
    }

    /**
     * 読み込み可能なソースのみをリストする
     */
    public List<ReadableTextSource> list() {
        ArrayList<ReadableTextSource> list = new ArrayList<>();
        for (JavaSource javaSource : javaSources) {
            try {
                list.add(javaSource.toReadableTextSource());
            } catch (IOException e) {
                logger.warn("cannot read {} (skip)", javaSource.location());
            }
        }
        return list;
    }
}
