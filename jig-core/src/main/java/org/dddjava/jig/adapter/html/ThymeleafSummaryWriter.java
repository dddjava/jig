package org.dddjava.jig.adapter.html;

import org.dddjava.jig.application.JigDocumentWriter;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.module.JigPackage;
import org.dddjava.jig.domain.model.information.module.JigPackageWithJigTypes;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.file.Path;
import java.util.*;

import static java.util.stream.Collectors.*;

/**
 * Thymeleafを使用して概要HTMLを出力する
 */
public class ThymeleafSummaryWriter {

    protected final JigDocumentContext jigDocumentContext;
    private final TemplateEngine templateEngine;

    public ThymeleafSummaryWriter(TemplateEngine templateEngine, JigDocumentContext jigDocumentContext) {
        this.templateEngine = templateEngine;
        this.jigDocumentContext = jigDocumentContext;
    }

    public List<Path> write(JigDocument jigDocument, SummaryModel summaryModel) {
        JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());
        if (summaryModel.empty()) {
            jigDocumentWriter.markSkip();
            return List.of();
        }

        JigTypes jigTypes = summaryModel.jigTypes();
        List<JigPackageWithJigTypes> jigPackageWithJigTypes = JigPackageWithJigTypes.from(jigTypes);
        Map<PackageIdentifier, Set<PackageIdentifier>> packageMap = jigPackageWithJigTypes.stream()
                .map(JigPackageWithJigTypes::packageIdentifier)
                .flatMap(packageIdentifier -> packageIdentifier.genealogical().stream())
                .collect(groupingBy(packageIdentifier -> packageIdentifier.parent(), toSet()));
        var baseComposite = createTreeBaseComposite(jigTypes, packageMap);

        List<JigPackage> jigPackages = packageMap.values().stream()
                .flatMap(Set::stream)
                .sorted(Comparator.comparing(PackageIdentifier::asText))
                .map(packageIdentifier -> jigPackage(packageIdentifier))
                .collect(toList());

        var contextMap = Map.of(
                "baseComposite", baseComposite,
                "jigPackages", jigPackages,
                "jigTypes", jigTypes.list(),
                "model", summaryModel,
                "title", jigDocumentWriter.jigDocument().label()
        );

        Context context = new Context(Locale.ROOT, contextMap);
        context.setVariables(summaryModel.additionalMap());
        String template = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.writeTextAs(".html",
                writer -> templateEngine.process(template, context, writer));

        return jigDocumentWriter.outputFilePaths();
    }

    private TreeComposite createTreeBaseComposite(JigTypes jigTypes, Map<PackageIdentifier, Set<PackageIdentifier>> packageMap) {
        TreeComposite baseComposite = new TreeComposite(jigPackage(PackageIdentifier.defaultPackage()));
        createTree(jigTypes, packageMap, baseComposite);
        return baseComposite;
    }

    private void createTree(JigTypes jigTypes,
                            Map<PackageIdentifier, Set<PackageIdentifier>> packageMap,
                            TreeComposite baseComposite) {
        for (PackageIdentifier current : packageMap.getOrDefault(baseComposite.packageIdentifier(), Collections.emptySet())) {
            TreeComposite composite = new TreeComposite(jigPackage(current));
            // add package
            baseComposite.addComponent(composite);
            // add class
            for (JigType jigType : jigTypes.listMatches(jigType -> jigType.packageIdentifier().equals(current))) {
                composite.addComponent(new TreeLeaf(jigType));
            }
            createTree(jigTypes, packageMap, composite);
        }
    }

    private JigPackage jigPackage(PackageIdentifier packageIdentifier) {
        return new JigPackage(packageIdentifier, jigDocumentContext.packageTerm(packageIdentifier));
    }
}
