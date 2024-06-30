package org.dddjava.jig.domain.model.sources.additional;

import java.util.List;
import java.util.regex.Pattern;

public record JavadocHtmlConverter(List<String> imports) {
    private static final Pattern LINK_TAG = Pattern.compile("\\{@link *([\\w.#()]+) *(\\w)}");

    public String convertToHtml(String javadocText) {


        return null;
    }
}
