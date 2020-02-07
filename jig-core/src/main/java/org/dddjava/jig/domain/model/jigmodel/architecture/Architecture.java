package org.dddjava.jig.domain.model.jigmodel.architecture;

import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotation;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.jigloaded.relation.packages.PackageRelation;
import org.dddjava.jig.domain.model.jigmodel.relation.RoundingPackageRelations;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * アーキテクチャ
 */
public class Architecture {

    IsBusinessRule isBusinessRule;

    public Architecture(IsBusinessRule isBusinessRule) {
        this.isBusinessRule = isBusinessRule;
    }

    boolean isService(List<TypeAnnotation> typeAnnotations) {
        TypeIdentifier serviceAnnotation = new TypeIdentifier("org.springframework.stereotype.Service");
        return typeAnnotations.stream()
                .anyMatch(typeAnnotation -> typeAnnotation.typeIs(serviceAnnotation));
    }

    boolean isDataSource(TypeByteCode typeByteCode) {
        // TODO インタフェース実装を見てない
        // DataSourceは Repositoryインタフェースが実装され @Repository のついた infrastructure/datasource のクラス
        List<TypeAnnotation> typeAnnotations = typeByteCode.typeAnnotations();
        TypeIdentifier repositoryAnnotation = new TypeIdentifier("org.springframework.stereotype.Repository");
        return typeAnnotations.stream()
                .anyMatch(typeAnnotation -> typeAnnotation.typeIs(repositoryAnnotation));
    }

    boolean isController(List<TypeAnnotation> typeAnnotations) {
        TypeIdentifier controller = new TypeIdentifier("org.springframework.stereotype.Controller");
        TypeIdentifier restController = new TypeIdentifier("org.springframework.web.bind.annotation.RestController");
        TypeIdentifier controllerAdvice = new TypeIdentifier("org.springframework.web.bind.annotation.ControllerAdvice");
        return typeAnnotations.stream()
                .anyMatch(typeAnnotation -> typeAnnotation.typeIs(controller)
                        || typeAnnotation.typeIs(restController)
                        || typeAnnotation.typeIs(controllerAdvice));
    }

    public boolean isBusinessRule(TypeByteCode typeByteCode) {
        return isBusinessRule.isBusinessRule(typeByteCode);
    }

    public RoundingPackageRelations toRoundingPackageRelations(TypeByteCodes typeByteCodes) {
        Pattern protagonistPattern = Pattern.compile(".*\\.(application|domain|infrastructure|presentation)\\..*");

        ArrayList<PackageRelation> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            TypeIdentifiers typeIdentifiers = typeByteCode.useTypes();
            PackageIdentifier fromPackage = packageIdentifier(protagonistPattern, typeByteCode.typeIdentifier());

            for (TypeIdentifier toTypeIdentifier : typeIdentifiers.list()) {
                if (toTypeIdentifier.isJavaLanguageType()) {
                    // 興味のない関連
                    continue;
                }
                PackageIdentifier toPackage = packageIdentifier(protagonistPattern, toTypeIdentifier);

                if (fromPackage.equals(toPackage)) {
                    // 自己参照
                    continue;
                }
                PackageRelation e = new PackageRelation(fromPackage, toPackage);
                if (!list.contains(e)) {
                    list.add(e);
                }
            }
        }
        return new RoundingPackageRelations(list);
    }

    private PackageIdentifier packageIdentifier(Pattern protagonistPattern, TypeIdentifier typeIdentifier) {
        Matcher matcher = protagonistPattern.matcher(typeIdentifier.fullQualifiedName());
        if (matcher.matches()) {
            String protagonistName = matcher.group(1);
            return new PackageIdentifier(protagonistName);
        }

        String fqn = typeIdentifier.fullQualifiedName();
        // 3階層までに丸める
        String[] split = fqn.split("\\.");
        String name = Arrays.stream(split)
                .limit(split.length < 4 ? split.length - 1 : 3)
                .collect(Collectors.joining("."));
        return new PackageIdentifier(name);
    }
}
