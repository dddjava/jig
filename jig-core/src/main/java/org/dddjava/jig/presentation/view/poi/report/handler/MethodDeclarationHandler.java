package org.dddjava.jig.presentation.view.poi.report.handler;

import org.dddjava.jig.domain.basic.ReportItem;
import org.dddjava.jig.domain.basic.Text;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.japanese.JapaneseName;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;

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
            case メソッド名:
                return methodDeclaration.asSignatureSimpleText();
            case メソッド和名:
                return convertContext.glossaryService.japaneseNameFrom(methodDeclaration.identifier()).summarySentence();
            case メソッド戻り値の型:
                return methodDeclaration.returnType().asSimpleText();
            case メソッド戻り値の型の和名:
                return convertContext.glossaryService.japaneseNameFrom(methodDeclaration.returnType()).summarySentence();
            case メソッド引数の型の和名:
                return methodDeclaration.methodSignature().arguments().stream()
                        .map(convertContext.glossaryService::japaneseNameFrom)
                        .map(JapaneseName::summarySentence)
                        .collect(Text.collectionCollector());
        }

        throw new IllegalArgumentException();
    }
}
