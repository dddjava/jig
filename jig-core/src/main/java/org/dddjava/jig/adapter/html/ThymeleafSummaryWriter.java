package org.dddjava.jig.adapter.html;

import org.dddjava.jig.application.JigDocumentWriter;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.documents.summaries.SummaryModel;
import org.dddjava.jig.domain.model.information.domains.categories.CategoryType;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypeValueKind;
import org.dddjava.jig.domain.model.information.jigobject.package_.JigPackage;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

/**
 * 概要View
 *
 * 概要HTMLで出力するパッケージツリーと詳細のモデルを加工します。
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

        Map<PackageIdentifier, List<JigType>> jigTypeMap = summaryModel.map();
        Map<PackageIdentifier, Set<PackageIdentifier>> packageMap = jigTypeMap.keySet().stream()
                .flatMap(packageIdentifier -> packageIdentifier.genealogical().stream())
                .collect(groupingBy(packageIdentifier -> packageIdentifier.parent(), toSet()));
        TreeComposite baseComposite = new TreeComposite(jigDocumentContext.jigPackage(PackageIdentifier.defaultPackage()));
        createTree(jigTypeMap, packageMap, baseComposite);
        List<JigType> jigTypes = jigTypeMap.values().stream().flatMap(List::stream)
                .sorted(Comparator.comparing(JigType::fqn))
                .collect(toList());
        List<JigPackage> jigPackages = packageMap.values().stream()
                .flatMap(Set::stream)
                .sorted(Comparator.comparing(PackageIdentifier::asText))
                .map(packageIdentifier -> jigDocumentContext.jigPackage(packageIdentifier))
                .collect(toList());
        Map<TypeIdentifier, CategoryType> categoriesMap = jigTypes.stream()
                .filter(jigType -> jigType.toValueKind() == JigTypeValueKind.区分)
                .map(CategoryType::new)
                .collect(toMap(CategoryType::typeIdentifier, Function.identity()));

        var contextMap = Map.of(
                "baseComposite", baseComposite,
                "jigPackages", jigPackages,
                "jigTypes", jigTypes,
                "categoriesMap", categoriesMap,
                "enumModels", summaryModel.enumModels(),
                "model", summaryModel
        );
        write(jigDocumentWriter, contextMap);

        return jigDocumentWriter.outputFilePaths();
    }

    private void createTree(Map<PackageIdentifier, List<JigType>> jigTypeMap,
                            Map<PackageIdentifier, Set<PackageIdentifier>> packageMap,
                            TreeComposite baseComposite) {
        for (PackageIdentifier current : packageMap.getOrDefault(baseComposite.packageIdentifier(), Collections.emptySet())) {
            TreeComposite composite = new TreeComposite(jigDocumentContext.jigPackage(current));
            // add package
            baseComposite.addComponent(composite);
            // add class
            for (JigType jigType : jigTypeMap.getOrDefault(current, Collections.emptyList())) {
                composite.addComponent(new TreeLeaf(jigType));
            }
            createTree(jigTypeMap, packageMap, composite);
        }
    }

    private void write(JigDocumentWriter jigDocumentWriter, Map<String, Object> contextMap) {
        contextMap.put("title", jigDocumentWriter.jigDocument().label());
        Context context = new Context(Locale.ROOT, contextMap);
        String template = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.writeTextAs(".html",
                writer -> templateEngine.process(template, context, writer));
    }
}
