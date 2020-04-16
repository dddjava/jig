package org.dddjava.jig.domain.model.jigpresentation.architectures;

import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.jigloaded.relation.packages.PackageRelation;
import org.dddjava.jig.domain.model.jigloaded.relation.packages.PackageRelations;
import org.dddjava.jig.domain.model.jigloader.architecture.Architecture;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RoundingPackageRelations {

    List<PackageRelation> list;

    RoundingPackageRelations(List<PackageRelation> list) {
        this.list = list;
    }

    public static RoundingPackageRelations form(TypeByteCodes typeByteCodes, Architecture architecture) {
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

    private static PackageIdentifier packageIdentifier(Pattern protagonistPattern, TypeIdentifier typeIdentifier) {
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

    public boolean worthless() {
        return list.isEmpty();
    }

    public PackageRelations packageRelations() {
        return new PackageRelations(list);
    }
}
