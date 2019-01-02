package org.dddjava.jig.presentation.view.poi.report.handler;

import org.dddjava.jig.domain.model.implementation.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.implementation.japanese.JapaneseName;
import org.dddjava.jig.domain.type.text.Text;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

public class MethodDeclarationHandler implements ItemHandler {

    ConvertContext convertContext;

    public MethodDeclarationHandler(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canHandle(Object obj) {
        return obj instanceof MethodDeclaration;
    }

    @Override
    public String handle(ReportItem item, Object obj) {
        MethodDeclaration methodDeclaration = (MethodDeclaration) obj;
        switch (item) {
            case クラス名:
            case クラス和名:
                return new TypeIdentifierHandler(convertContext).handle(item, methodDeclaration.declaringType());
            case メソッドシグネチャ:
                return methodDeclaration.asSignatureSimpleText();
            case メソッド和名:
                return convertContext.glossaryService.japaneseNameFrom(methodDeclaration.identifier()).summarySentence();
            case メソッド戻り値の型:
                return methodDeclaration.methodReturn().typeIdentifier().asSimpleText();
            case メソッド戻り値の型の和名:
                return convertContext.glossaryService.japaneseNameFrom(methodDeclaration.methodReturn().typeIdentifier()).summarySentence();
            case メソッド引数の型の和名:
                return methodDeclaration.methodSignature().arguments().stream()
                        .map(convertContext.glossaryService::japaneseNameFrom)
                        .map(JapaneseName::summarySentence)
                        .collect(Text.collectionCollector());
        }

        throw new IllegalArgumentException(item.name());
    }
}
