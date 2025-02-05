package org.dddjava.jig.domain.model.data.term;

import org.dddjava.jig.domain.model.data.classes.method.JavaMethodDeclarator;
import org.dddjava.jig.domain.model.data.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 用語
 */
public record Term(TermIdentifier identifier, String title, String description, TermKind termKind,
                   Object additionalInformation) {
    public Term(TermIdentifier identifier, String title, String description, TermKind termKind) {
        this(identifier, title, description.trim(), termKind, null);
    }

    public static Term fromPackage(PackageIdentifier packageIdentifier, String javadocDescriptionText) {
        var text = JavadocParser.normalize(javadocDescriptionText);
        var title = JavadocParser.summaryText(text);
        return new Term(new TermIdentifier(packageIdentifier.asText()), title, JavadocParser.bodyText(title, text), TermKind.パッケージ);
    }

    public static Term fromClass(TypeIdentifier typeIdentifier, String javadocDescriptionText) {
        var text = JavadocParser.normalize(javadocDescriptionText);
        var title = JavadocParser.summaryText(text);
        return new Term(new TermIdentifier(typeIdentifier.fullQualifiedName()), title, JavadocParser.bodyText(title, text), TermKind.クラス);
    }

    public static Term fromMethod(TypeIdentifier typeIdentifier, JavaMethodDeclarator javaMethodDeclarator, String javadocDescriptionText) {
        var text = JavadocParser.normalize(javadocDescriptionText);
        var title = JavadocParser.summaryText(text);
        return new Term(new TermIdentifier(typeIdentifier.fullQualifiedName() + "#" + javaMethodDeclarator.asText()),
                title, JavadocParser.bodyText(title, text), TermKind.メソッド, javaMethodDeclarator);
    }

    public static Term defaultMethodTerm(MethodIdentifier identifier) {
        return new Term(new TermIdentifier(identifier.asText()), identifier.asSimpleText(), "", TermKind.メソッド);
    }

    private static class JavadocParser {

        /**
         * インラインのlinkタグをテキストにするためのパターン
         */
        private static final Pattern INLINETAG_LINK_PATTERN = Pattern.compile("\\{@link\\s+(?:\\S+\\s+)?(\\S+)\\s*}");

        /**
         * 改行コードを統一するためのパターン。
         * Javaparserを使用する場合、ソースの改行コードに関わらずline.separatorに置き換えられる。JIGの出力は\nに寄せるので、ここで一律置き換える。
         */
        private static final Pattern LINE_SEPARATOR_PATTERN = Pattern.compile("\\R");

        static String normalize(String javadocDescriptionText) {
            return INLINETAG_LINK_PATTERN.matcher(
                    LINE_SEPARATOR_PATTERN.matcher(javadocDescriptionText).replaceAll("\n")
            ).replaceAll("$1");
        }

        static String summaryText(String value) {
            if (value.isEmpty()) {
                return "";
            }

            // 改行や句点の手前まで。
            return Stream.of(value.indexOf("\r\n"), value.indexOf("\n"), value.indexOf("。"))
                    .filter(length -> length >= 0)
                    .min(Integer::compareTo)
                    .map(end -> value.substring(0, end))
                    // 改行も句点も無い場合はそのまま返す
                    .orElse(value);
        }

        static String bodyText(String title, String value) {
            // titleとvalueが同じなら本文なし
            // 改行や句点を除くために+1
            int beginIndex = title.length() + 1;
            if (value.length() <= beginIndex) {
                return "";
            }

            return value.substring(beginIndex).trim();
        }
    }
}
