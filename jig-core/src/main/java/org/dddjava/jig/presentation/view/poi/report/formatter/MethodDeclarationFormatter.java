package org.dddjava.jig.presentation.view.poi.report.formatter;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.text.Text;
import org.dddjava.jig.domain.model.fact.alias.TypeAlias;
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
            case クラス別名:
                return new TypeIdentifierFormatter(convertContext).format(itemCategory, methodDeclaration.declaringType());
            case メソッドシグネチャ:
                return methodDeclaration.asSignatureSimpleText();
            case メソッド別名:
                return convertContext.aliasService.methodAliasOf(methodDeclaration.identifier()).asText();
            case メソッド戻り値の型:
                return methodDeclaration.methodReturn().typeIdentifier().asSimpleText();
            case メソッド戻り値の型の別名:
                return convertContext.aliasService.typeAliasOf(methodDeclaration.methodReturn().typeIdentifier()).asText();
            case メソッド引数の型の別名:
                List<TypeAlias> list = methodDeclaration.methodSignature().arguments().stream()
                        .map(convertContext.aliasService::typeAliasOf)
                        .collect(toList());
                return Text.of(list, alias -> alias.asText());
        }

        throw new IllegalArgumentException(itemCategory.name());
    }
}
