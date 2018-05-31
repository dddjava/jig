package org.dddjava.jig.domain.model.services;

import org.dddjava.jig.domain.basic.report.ConvertibleItem;
import org.dddjava.jig.domain.basic.report.Report;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.japanese.JapaneseName;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;

/**
 * サービスレポート
 */
public class ServiceReport {

    /**
     * レポート項目
     */
    private enum Items implements ConvertibleItem<Row> {
        クラス名(Row::クラス名),
        クラス和名(Row::クラス和名),
        メソッド(Row::メソッド),
        メソッド戻り値の型(Row::メソッド戻り値の型),
        イベントハンドラ(Row::イベントハンドラ),
        メソッド和名(Row::メソッド和名),
        メソッド戻り値の型の和名(Row::返す型の和名),
        メソッド引数の型の和名(Row::引数の型の和名),
        使用しているフィールドの型(Row::使用しているフィールドの型),
        使用しているリポジトリのメソッド(Row::使用しているリポジトリのメソッド);

        Function<Row, String> func;

        Items(Function<Row, String> func) {
            this.func = func;
        }

        @Override
        public String convert(Row row) {
            return func.apply(row);
        }
    }

    private final List<Row> list;

    public ServiceReport(List<Row> list) {
        this.list = list;
    }

    public Report<?> toReport() {
        return new Report<>("SERVICE", list, Items.values());
    }

    public static class Row {
        ServiceAngle serviceAngle;
        Function<TypeIdentifier, JapaneseName> japaneseNameResolver;
        Function<MethodDeclaration, JapaneseName> methodJapaneseNameResolver;
        TypeIdentifierFormatter identifierFormatter;

        public Row(ServiceAngle serviceAngle,
                   Function<TypeIdentifier, JapaneseName> japaneseNameResolver,
                   Function<MethodDeclaration, JapaneseName> methodJapaneseNameResolver,
                   TypeIdentifierFormatter identifierFormatter) {
            this.serviceAngle = serviceAngle;
            this.japaneseNameResolver = japaneseNameResolver;
            this.methodJapaneseNameResolver = methodJapaneseNameResolver;
            this.identifierFormatter = identifierFormatter;
        }

        String クラス名() {
            return serviceAngle.method().declaringType().format(identifierFormatter);
        }

        String クラス和名() {
            return japaneseNameResolver.apply(serviceAngle.method().declaringType()).summarySentence();
        }

        String メソッド() {
            return serviceAngle.method().asSignatureSimpleText();
        }

        String メソッド戻り値の型() {
            return serviceAngle.returnType().asSimpleText();
        }

        String イベントハンドラ() {
            return serviceAngle.usingFromController().toSymbolText();
        }

        String メソッド和名() {
            return methodJapaneseNameResolver.apply(serviceAngle.method()).summarySentence();
        }

        String 返す型の和名() {
            return japaneseNameResolver.apply(serviceAngle.method().returnType()).summarySentence();
        }

        String 引数の型の和名() {
            return serviceAngle.method().methodSignature().arguments().stream()
                    .map(japaneseNameResolver)
                    .map(JapaneseName::summarySentence)
                    .collect(joining(" ,", "[", "]"));
        }

        String 使用しているフィールドの型() {
            return serviceAngle.usingFields().asSimpleText();
        }

        String 使用しているリポジトリのメソッド() {
            return serviceAngle.usingRepositoryMethods().asSimpleText();
        }
    }
}
