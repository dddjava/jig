package org.dddjava.jig.domain.model.jigdocumenter.architectures;

import org.dddjava.jig.domain.model.jigmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.relation.class_.ClassRelation;
import org.dddjava.jig.domain.model.jigmodel.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigmodel.relation.packages.PackageRelation;
import org.dddjava.jig.domain.model.jigmodel.relation.packages.PackageRelations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * アーキテクチャー単位に丸めたパッケージ関連
 */
public class RoundingPackageRelations {

    List<PackageRelation> list;

    public RoundingPackageRelations(List<PackageRelation> list) {
        this.list = list;
    }

    public static RoundingPackageRelations getRoundingPackageRelations(ClassRelations classRelations) {
        ArrayList<PackageRelation> list = new ArrayList<>();
        for (ClassRelation classRelation : classRelations.list()) {
            TypeIdentifier from = classRelation.from();
            TypeIdentifier to = classRelation.to();

            PackageIdentifier fromPackage = packageIdentifier(from);

            if (to.isJavaLanguageType()) {
                // 興味のない関連
                continue;
            }
            PackageIdentifier toPackage = packageIdentifier(to);

            if (fromPackage.equals(toPackage)) {
                // 自己参照
                continue;
            }
            PackageRelation e = new PackageRelation(fromPackage, toPackage);
            if (!list.contains(e)) {
                list.add(e);
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

    public boolean worthless() {
        return list.isEmpty();
    }

    public PackageRelations packageRelations() {
        return new PackageRelations(list);
    }
}
