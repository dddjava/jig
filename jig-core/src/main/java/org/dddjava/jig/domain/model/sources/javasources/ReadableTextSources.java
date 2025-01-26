package org.dddjava.jig.domain.model.sources.javasources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadableTextSources {
    static Logger logger = LoggerFactory.getLogger(ReadableTextSources.class);

    private final List<TextSource> textSources;

    public ReadableTextSources(List<TextSource> textSources) {
        this.textSources = textSources;
    }

    /**
     * 読み込み可能なソースのみをリストする
     */
    public List<ReadableTextSource> list() {
        ArrayList<ReadableTextSource> list = new ArrayList<>();
        for (TextSource textSource : textSources) {
            try {
                list.add(textSource.toReadableTextSource());
            } catch (IOException e) {
                logger.warn("cannot read {} (skip)", textSource.location());
            }
        }
        return list;
    }
}
