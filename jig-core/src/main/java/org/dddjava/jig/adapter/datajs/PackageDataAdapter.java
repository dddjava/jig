package org.dddjava.jig.adapter.datajs;

import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelations;
import org.dddjava.jig.domain.model.knowledge.module.JigPackages;

import java.util.List;

/**
 * パッケージ関連（package-data.js）
 */
public class PackageDataAdapter implements JigDocumentDataAdapter {

    private final JigService jigService;

    public PackageDataAdapter(JigService jigService) {
        this.jigService = jigService;
    }

    @Override
    public String variableName() {
        return "packageData";
    }

    @Override
    public String dataFileName() {
        return "package-data";
    }

    @Override
    public String buildJson(JigRepository jigRepository) {
        var jigPackages = jigService.packages(jigRepository);
        var packageRelations = jigService.packageRelations(jigRepository);
        var domainPackageRoots = jigService.coreDomainJigTypes(jigRepository).domainPackageRoots();
        return buildPackageJson(jigPackages, packageRelations, domainPackageRoots);
    }

    public static String buildPackageJson(JigPackages jigPackages, PackageRelations packageRelations, List<String> domainPackageRoots) {
        return Json.object("packages", Json.arrayObjects(jigPackages.listPackage().stream()
                        .map(packageInfo -> Json.object("fqn", packageInfo.fqn())
                                .and("classCount", packageInfo.numberOfClasses()))
                        .toList()))
                .and("relations", Json.arrayObjects(packageRelations.listUnique().stream()
                        .map(relation -> Json.object("from", relation.from().asText())
                                .and("to", relation.to().asText()))
                        .toList()))
                .and("domainPackageRoots", Json.array(domainPackageRoots))
                .build();
    }
}
