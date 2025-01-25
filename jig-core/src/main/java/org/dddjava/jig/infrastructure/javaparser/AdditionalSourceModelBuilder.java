package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AdditionalSourceModelBuilder {
    private static final Logger logger = LoggerFactory.getLogger(AdditionalSourceModelBuilder.class);

    private PackageDeclaration packageDeclaration;

    public void setPackage(PackageDeclaration packageDeclaration) {
        if (this.packageDeclaration != null) {
            logger.warn("package: {} が設定されている状態でaddPackage {} が呼ばれました。" +
                            "通常このような構造はparse時にエラーとなるため、予期しない構造です。" +
                            "packageが正常に解釈されない可能性があります。",
                    this.packageDeclaration, packageDeclaration);
        }
        this.packageDeclaration = packageDeclaration;
    }

    public void addImport(ImportDeclaration importDeclaration) {
    }

    public void setTypeName(String name) {
    }
}
