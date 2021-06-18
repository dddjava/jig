package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

class JigExpressionObject {
    private JigDocumentContext jigDocumentContext;

    public JigExpressionObject(JigDocumentContext jigDocumentContext) {
        this.jigDocumentContext = jigDocumentContext;
    }

    public String labelText(TypeIdentifier typeIdentifier) {
        ClassComment classComment = jigDocumentContext.classComment(typeIdentifier);
        return classComment.asTextOrIdentifierSimpleText();
    }
}
