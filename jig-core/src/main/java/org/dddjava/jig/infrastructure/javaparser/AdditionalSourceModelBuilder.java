package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.additional.AdditionalTypeModel;
import org.dddjava.jig.domain.model.sources.additional.TypeAlias;
import org.dddjava.jig.domain.model.sources.additional.TypeDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class AdditionalSourceModelBuilder {
    private static final Logger logger = LoggerFactory.getLogger(AdditionalSourceModelBuilder.class);

    private final List<ImportDeclaration> importDeclarations = new ArrayList<>();

    private PackageDeclaration packageDeclaration;
    private String name;

    public AdditionalTypeModel build() {
        var typeIdentifier = TypeIdentifier.valueOf(packageDeclaration != null
                ? packageDeclaration.getNameAsString() + '.' + name
                : name);
        var imports = importDeclarations.stream()
                .map(importDeclaration -> importDeclaration.getNameAsString())
                .toList();

        return new AdditionalTypeModel(
                typeIdentifier,
                imports,
                new TypeAlias(typeIdentifier, ""),
                new TypeDescription(typeIdentifier, ""),
                List.of()
        );
    }

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
        importDeclarations.add(importDeclaration);
    }

    public void setTypeName(String name) {
        this.name = name;
    }
}
