package org.dddjava.jig.domain.model.jigmodel.lowmodel.alias;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodIdentifier;

/**
 * メソッド別名
 */
public class MethodAlias {
    MethodIdentifier methodIdentifier;
    DocumentationComment documentationComment;

    public MethodAlias(MethodIdentifier methodIdentifier, DocumentationComment documentationComment) {
        this.methodIdentifier = methodIdentifier;
        this.documentationComment = documentationComment;
    }

    public static MethodAlias empty(MethodIdentifier methodIdentifier) {
        return new MethodAlias(methodIdentifier, DocumentationComment.empty());
    }

    public MethodIdentifier methodIdentifier() {
        return methodIdentifier;
    }

    public String asText() {
        return documentationComment.summaryText();
    }

    public String asTextOrDefault(String defaultText) {
        if (documentationComment.exists()) {
            return asText();
        }
        return defaultText;
    }

    public boolean isAliasFor(MethodIdentifier methodIdentifier) {
        // TODO オーバーロードに対応するときはここでやる。
        // MethodAliasのフィールドを硬いmethodIdentifierを持つのではなく、引数も含めてある程度マッチする感じのやつを作る。
        // ここを対応したら matchesIgnoreOverloadメソッドはいらなくなる
        return methodIdentifier.matchesIgnoreOverload(this.methodIdentifier);
    }

    public String descriptionText() {
        return documentationComment.fullText();
    }

    public boolean exists() {
        return documentationComment.exists();
    }
}
