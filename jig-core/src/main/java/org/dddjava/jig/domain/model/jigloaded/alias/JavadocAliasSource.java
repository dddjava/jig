package org.dddjava.jig.domain.model.jigloaded.alias;

import java.util.stream.Stream;

public class JavadocAliasSource {

    String value;

    public JavadocAliasSource(String value) {
        this.value = value;
    }

    public Alias toAlias() {
        int end = Stream.of(value.indexOf("\n"), value.indexOf("。"), value.length())
                .filter(length -> length >= 0)
                .min(Integer::compareTo).orElseThrow(IllegalStateException::new);
        return new Alias(value.substring(0, end));
    }
}
