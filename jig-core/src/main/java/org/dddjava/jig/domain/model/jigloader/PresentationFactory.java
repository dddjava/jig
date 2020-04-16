package org.dddjava.jig.domain.model.jigloader;

import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.jigloaded.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigloaded.relation.packages.PackageRelation;
import org.dddjava.jig.domain.model.jigloader.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.jigloader.architecture.Architecture;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceMethods;
import org.dddjava.jig.domain.model.jigmodel.businessrules.CategoryTypes;
import org.dddjava.jig.domain.model.jigpresentation.architectures.RoundingPackageRelations;
import org.dddjava.jig.domain.model.jigpresentation.diagram.CategoryUsageDiagram;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PresentationFactory {
    public static RoundingPackageRelations roundingPackageRelations(TypeByteCodes typeByteCodes) {

        ArrayList<PackageRelation> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            TypeIdentifiers typeIdentifiers = typeByteCode.useTypes();
            PackageIdentifier fromPackage = packageIdentifier(typeByteCode.typeIdentifier());

            for (TypeIdentifier toTypeIdentifier : typeIdentifiers.list()) {
                if (toTypeIdentifier.isJavaLanguageType()) {
                    // 興味のない関連
                    continue;
                }
                PackageIdentifier toPackage = packageIdentifier(toTypeIdentifier);

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

    private static PackageIdentifier packageIdentifier(TypeIdentifier typeIdentifier) {
        Pattern protagonistPattern = Pattern.compile(".*\\.(application|domain|infrastructure|presentation)\\..*");

        Matcher matcher = protagonistPattern.matcher(typeIdentifier.fullQualifiedName());
        if (matcher.matches()) {
            String protagonistName = matcher.group(1);
            return new PackageIdentifier(protagonistName);
        }

        String fqn = typeIdentifier.fullQualifiedName();
        // 2階層までに丸める
        String[] split = fqn.split("\\.");
        String name = Arrays.stream(split)
                .limit(split.length < 3 ? split.length - 1 : 2)
                .collect(Collectors.joining("."));
        return new PackageIdentifier(name);
    }

    public static CategoryUsageDiagram createCategoryUsageDiagram(CategoryTypes categoryTypes, AnalyzedImplementation analyzedImplementation, Architecture architecture) {
        ServiceMethods serviceMethods = MethodFactory.createServiceMethods(analyzedImplementation.typeByteCodes(), architecture);
        ClassRelations classRelations = RelationsFactory.createClassRelations(
                new TypeByteCodes(analyzedImplementation.typeByteCodes().list()
                        .stream()
                        .filter(typeByteCode -> architecture.isBusinessRule(typeByteCode))
                        .collect(Collectors.toList()))
        );

        return new CategoryUsageDiagram(serviceMethods, categoryTypes, classRelations);
    }
}
