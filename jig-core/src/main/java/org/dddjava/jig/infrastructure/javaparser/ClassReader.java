package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import org.dddjava.jig.domain.model.jigsource.file.text.javacode.JavaSource;

class ClassReader {

    TypeSourceResult read(JavaSource javaSource) {
        CompilationUnit cu = StaticJavaParser.parse(javaSource.toInputStream());

        String packageName = cu.getPackageDeclaration()
                .map(PackageDeclaration::getNameAsString)
                .map(name -> name + ".")
                .orElse("");

        ClassVisitor typeVisitor = new ClassVisitor(packageName);
        cu.accept(typeVisitor, null);

        return typeVisitor.toTypeSourceResult();
    }
}
