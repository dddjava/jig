package org.dddjava.jig.presentation.view.poi.report.formatter;

import org.dddjava.jig.domain.model.implementation.analyzed.alias.JapaneseName;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.type.text.Text;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

import java.util.List;

import static java.util.stream.Collectors.toList;

class MethodDeclarationFormatter implements ReportItemFormatter {

    ConvertContext convertContext;

    MethodDeclarationFormatter(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canFormat(Object item) {
        return item instanceof MethodDeclaration;
    }

    @Override
    public String format(ReportItem itemCategory, Object item) {
        MethodDeclaration methodDeclaration = (MethodDeclaration) item;
        switch (itemCategory) {
            case クラス名:
            case クラス和名:
                return new TypeIdentifierFormatter(convertContext).format(itemCategory, methodDeclaration.declaringType());
            case メソッドシグネチャ:
                return methodDeclaration.asSignatureSimpleText();
            case メソッド和名:
                return convertContext.glossaryService.japaneseNameFrom(methodDeclaration.identifier()).summarySentence();
            case メソッド戻り値の型:
                return methodDeclaration.methodReturn().typeIdentifier().asSimpleText();
            case メソッド戻り値の型の和名:
                return convertContext.glossaryService.japaneseNameFrom(methodDeclaration.methodReturn().typeIdentifier()).summarySentence();
            case メソッド引数の型の和名:
                List<JapaneseName> list = methodDeclaration.methodSignature().arguments().stream()
                        .map(convertContext.glossaryService::japaneseNameFrom)
                        .collect(toList());
                return Text.of(list, JapaneseName::summarySentence);
        }

        throw new IllegalArgumentException(itemCategory.name());
    }
}
