package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

class ClassReader {

    TypeSourceResult read(Path path) {
        try {
            CompilationUnit cu = JavaParser.parse(path);

            String packageName = cu.getPackageDeclaration()
                    .map(PackageDeclaration::getNameAsString)
                    .map(name -> name + ".")
                    .orElse("");

            ClassVisitor typeVisitor = new ClassVisitor(packageName);
            cu.accept(typeVisitor, null);

            return typeVisitor.toTypeSourceResult();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
