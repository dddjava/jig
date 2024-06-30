package org.dddjava.jig.domain.model.sources.additional;

import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

/**
 * 型に対する説明
 */
public record TypeDescription(TypeIdentifier typeIdentifier, String descriptionSource) {

    public String descriptionHtml(JavadocHtmlConverter javadocConverter) {
        return javadocConverter.convertToHtml(descriptionSource());
    }
}
