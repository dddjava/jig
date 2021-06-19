package org.dddjava.jig.domain.model.sources.file.text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadableTextSources {
    static Logger logger = LoggerFactory.getLogger(ReadableTextSources.class);

    private List<CodeSource> codeSources;

    public ReadableTextSources(List<CodeSource> codeSources) {
        this.codeSources = codeSources;
    }

    public List<ReadableTextSource> list() {
        ArrayList<ReadableTextSource> list = new ArrayList<>();
        for (CodeSource codeSource : codeSources) {
            try {
                byte[] bytes = codeSource.readAllBytes();
                list.add(new ReadableTextSource(codeSource, bytes));
            } catch (IOException e) {
                logger.warn("cannot read {} (skip)", codeSource.location());
            }
        }
        return list;
    }
}
